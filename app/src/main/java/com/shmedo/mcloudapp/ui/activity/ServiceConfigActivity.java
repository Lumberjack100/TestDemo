package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.AppContants;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.util.UserConfig;
import com.shmedo.mcloudapp.views.ClearEditText;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   ServiceConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/9 15:51
 * 描述：    配置服务器
 */
public class ServiceConfigActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.etServiceAddress)
    ClearEditText mEtServiceAddress;

    @BindView(R.id.btnServiceTest)
    Button mBtnServiceTest;

    private UserConfig userConfig;

    private String config = "mdnetservice.shmedo.cn";


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ServiceConfigActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_service_config;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("配置服务器");
        initServiceAddress();
    }


    private void initServiceAddress() {
        mEtServiceAddress.setText(config);
        userConfig = UserConfig.getConfig(this,  AppContants.APP_CONFIG_NAME);
        String address = userConfig.readString(AppContants.SERVICE_ADDRESS);
        if (!TextUtils.isEmpty(address)) {
            mEtServiceAddress.setText(address);
        }
    }


    @OnClick(R.id.btnServiceTest)
    public void onViewClicked() {
        final String service_text = mEtServiceAddress.getText().toString();
        if (TextUtils.isEmpty(service_text)) {
            mEtServiceAddress.setError("请输入配置服务器地址");
            mEtServiceAddress.requestFocus();
            return;
        }
        final UserConfig uc = UserConfig.getConfig(this,  AppContants.APP_CONFIG_NAME);
        uc.writeString(getResources().getString(R.string.service_address), service_text);
        MCloudApp.setHttpsServiceAddress(service_text);

        showLoadingDialog("正在配置服务器...");
        MDRetrofit.getInstance().createService().getApiVerson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String s, String message) {
                        dismissLoadingDialog();

                        ToastUtils.show("服务端已连接，API版本为：" + s);
                        userConfig.writeString(AppContants.SERVICE_ADDRESS, mEtServiceAddress.getText().toString());
                        finish();
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("服务端连接错误: " + message);
                        dismissLoadingDialog();

                        ToastUtils.show("服务端连接错误");
                    }
                });
    }

}
