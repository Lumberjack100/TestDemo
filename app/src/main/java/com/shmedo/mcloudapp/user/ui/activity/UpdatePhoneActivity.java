package com.shmedo.mcloudapp.user.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.UpdateMobileParam;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.MyCountDownTimer;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 修改手机号
 */
public class UpdatePhoneActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.newPhoneET)
    ClearEditText mEtNewPhone;

    @BindView(R.id.codeET)
    ClearEditText mEtCode;

    @BindView(R.id.getCodeBtn)
    TextView mBtnGetCode;

    private Handler hander = new Handler();

    private String newPhone, code;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, UpdatePhoneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_update_phone;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("修改手机号");
    }


    @OnClick({R.id.getCodeBtn, R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.getCodeBtn:
                newPhone = mEtNewPhone.getText().toString().trim();
                if (TextUtils.isEmpty(newPhone)) {
                    ToastUtils.show("请输入新手机号");
                    mEtNewPhone.requestFocus();
                    return;
                }
                if (!ValidateUtil.checkMobileNumber(newPhone)) {
                    ToastUtils.show("新手机号格式不正确！");
                    return;
                }

                sendSmsCode();
                break;

            case R.id.btn_confirm:
                preProcessParam();
                break;
        }
    }

    /**
     * 发送验证码
     */
    private void sendSmsCode() {
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, newPhone);

        showLoadingDialog("正在获取验证码...");
        MDRetrofit.getInstance()
                .createService()
                .sendSmsCode(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String data, String message) {
                        dismissLoadingDialog();

                        if (data.contains("已发送")) {
                            MyCountDownTimer timer = new MyCountDownTimer(mBtnGetCode, 60000, 1000);
                            timer.start();
                        } else {
                            ToastUtils.show(data);
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        dismissLoadingDialog();
                        ToastUtils.show(message);
                    }
                });
    }

    private void preProcessParam() {
        newPhone = mEtNewPhone.getText().toString();
        code = mEtCode.getText().toString();

        if (TextUtils.isEmpty(newPhone)) {
            ToastUtils.show("请输入新手机号");
            mEtNewPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(code)) {
            ToastUtils.show("请输入验证码");
            mEtCode.requestFocus();
            return;
        }

        if (!ValidateUtil.checkMobileNumber(newPhone)) {
            ToastUtils.show("新手机号格式不正确！");
            return;
        }

        if (!ValidateUtil.isNumeric(code)) {
            ToastUtils.show("验证码格式不正确！");
            return;
        }

        updateMyCellPhone();
    }

    /**
     * 修改手机号
     */
    private void updateMyCellPhone() {
        showLoadingDialog("处理中...");

        UpdateMobileParam parameter = new UpdateMobileParam();
        parameter.setNewCellPhone(newPhone);
        parameter.setCode(code);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);

        MDRetrofit.getInstance()
                .createService()
                .UpdateMyCellPhone(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String result, String message) {
                        dismissLoadingDialog();
                        ToastUtils.show("修改完成");

                        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
                        if (userInfo != null && userInfo.getUser() != null) {
                            UserInfo.UserBean user = userInfo.getUser();
                            user.setCellPhone(newPhone);
                        }
                        MCloudApp.setCurrentUserInfo(userInfo);

                        hander.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1500);
                    }

                    @Override
                    public void Failure(String message) {
                        dismissLoadingDialog();
                        ToastUtils.show(message);
                        Timber.w("请求失败--%s", message);
                    }
                });
    }
}
