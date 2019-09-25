package com.shmedo.mcloudapp.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.UserConfig;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.LoadingDialog;

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
    @BindView(R.id.etServiceAddress)
    ClearEditText mEtServiceAddress;
    @BindView(R.id.btnServiceTest)
    Button mBtnServiceTest;
    private UserConfig userConfig;
    private LoadingDialog dialog;
    private String config = "mdnetservice.shmedo.cn";

    @Override
    protected int initContentView() {
        return R.layout.activity_service_config;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ActionBar上显示返回
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initServiceAddress();

    }


    private void initServiceAddress() {
        dialog = new LoadingDialog(this);
        mEtServiceAddress.setText(config);
        userConfig = UserConfig.getConfig(this, CommonVariable.USER_CONFIG_NAME);
        String address = userConfig.readString(CommonVariable.SERVICE_ADDRESS);
        if (!StringUtil.isNullOrEmpty(address)) {
            mEtServiceAddress.setText(address);
        }
    }


    @OnClick(R.id.btnServiceTest)
    public void onViewClicked() {
        final String service_text = mEtServiceAddress.getText().toString();
        if (StringUtil.isNullOrEmpty(service_text)) {
            mEtServiceAddress.setError("请输入配置服务器地址");
            mEtServiceAddress.requestFocus();
            return;
        }
        final UserConfig uc = UserConfig.getConfig(this, CommonVariable.USER_CONFIG_NAME);
        uc.writeString(getResources().getString(R.string.service_address), service_text);
        CommonVariable.setServiceAddress(service_text);
        dialog.showCancelDialog("正在配置服务器...");
        MDRetrofit.getInstance().createService().getApiVerson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String s, String message) {
                        dialog.dismiss();
                        ToastUtil.showLToast("服务端已连接，API版本为：" + s);
                        userConfig.writeString(CommonVariable.SERVICE_ADDRESS, mEtServiceAddress.getText().toString());
                        finish();
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("服务端连接错误: " + message);
                        dialog.dismiss();
                        ToastUtil.showLToast("服务端连接错误");
                    }
                });
    }

}
