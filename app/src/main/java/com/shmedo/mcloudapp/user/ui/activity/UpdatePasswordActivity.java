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
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.UpdatePasswordParam;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.MD5Util;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

public class UpdatePasswordActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.oldPasswordET)
    ClearEditText mEtOldPassword;

    @BindView(R.id.newPasswordET)
    ClearEditText mEtNewPassword;

    @BindView(R.id.confirmNewPasswordET)
    ClearEditText mEtConfirmPassword;

    private Handler hander = new Handler();

    private String mAccount;
    private String oldPassword, newPassword, confirmNewPassword;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, UpdatePasswordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_update_password;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("修改密码");
        initDate();
    }

    private void initDate() {
        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            UserInfo.UserBean user = userInfo.getUser();
            mAccount = user.getAccount();
        }
    }


    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            oldPassword = mEtOldPassword.getText().toString();
            newPassword = mEtNewPassword.getText().toString();
            confirmNewPassword = mEtConfirmPassword.getText().toString();

            if (TextUtils.isEmpty(oldPassword)) {
                ToastUtils.show("请输入当前密码");
                mEtOldPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(newPassword)) {
                ToastUtils.show("请输入新密码");
                mEtNewPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(confirmNewPassword)) {
                ToastUtils.show("请输入确认密码");
                mEtConfirmPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                ToastUtils.show("确认密码与新密码不一致");
                return;
            }

            updatePassword();
        }
    }

    /**
     * 修改当前登录用户的密码
     */
    private void updatePassword() {
        showLoadingDialog("处理中...");

        UpdatePasswordParam parameter = new UpdatePasswordParam();
        parameter.setCurrentPassword(MD5Util.MD5(mAccount + oldPassword));
        parameter.setNewPassword(MD5Util.MD5(mAccount + newPassword));
        parameter.setConfirmNewPassword(MD5Util.MD5(mAccount + confirmNewPassword));

        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);

        MDRetrofit.getInstance()
                .createService()
                .ChangeMyPassword(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String result, String message) {
                        dismissLoadingDialog();
                        ToastUtils.show("修改完成");
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
