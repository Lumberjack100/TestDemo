package com.shmedo.mcloudapp.user.ui.activity;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.UpdatePasswordParam;
import com.shmedo.mcloudapp.util.ResponseHandler;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
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

    @BindView(R.id.iv_eye_old_password)
    ImageView mIvEyeOldPwd;

    @BindView(R.id.iv_eye_new_password)
    ImageView mIvEyeNewPwd;

    @BindView(R.id.iv_eye_confirm_password)
    ImageView mIvEyeConfirmPwd;

    private boolean isOldPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private String mAccount;

    private String oldPasswordMD5;

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
            oldPasswordMD5 = user.getPassword();
        }
    }


    @OnClick({R.id.iv_eye_old_password, R.id.iv_eye_new_password, R.id.iv_eye_confirm_password, R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_eye_old_password:
                if (!isOldPasswordVisible) {
                    isOldPasswordVisible = true;
                    mIvEyeOldPwd.setImageResource(R.drawable.icon_eye_open);
                    mEtOldPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    isOldPasswordVisible = false;
                    mIvEyeOldPwd.setImageResource(R.drawable.icon_eye_close);
                    mEtOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                mEtOldPassword.setSelection(mEtOldPassword.getText().toString().length());
                break;

            case R.id.iv_eye_new_password:
                if (!isNewPasswordVisible) {
                    isNewPasswordVisible = true;
                    mIvEyeNewPwd.setImageResource(R.drawable.icon_eye_open);
                    mEtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    isNewPasswordVisible = false;
                    mIvEyeNewPwd.setImageResource(R.drawable.icon_eye_close);
                    mEtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                mEtNewPassword.setSelection(mEtNewPassword.getText().toString().length());
                break;

            case R.id.iv_eye_confirm_password:
                if (!isConfirmPasswordVisible) {
                    isConfirmPasswordVisible = true;
                    mIvEyeConfirmPwd.setImageResource(R.drawable.icon_eye_open);
                    mEtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    isConfirmPasswordVisible = false;
                    mIvEyeConfirmPwd.setImageResource(R.drawable.icon_eye_close);
                    mEtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                mEtConfirmPassword.setSelection(mEtConfirmPassword.getText().toString().length());
                break;

            case R.id.btn_confirm:
                if (checkValue()) {
                    updatePassword();
                }
                break;
        }
    }

    private boolean checkValue() {
        oldPassword = mEtOldPassword.getText().toString();
        newPassword = mEtNewPassword.getText().toString();
        confirmNewPassword = mEtConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(oldPassword)) {
            ToastUtils.show("请输入当前密码");
            mEtOldPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            ToastUtils.show("请输入新密码");
            mEtNewPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(confirmNewPassword)) {
            ToastUtils.show("请输入确认密码");
            mEtConfirmPassword.requestFocus();
            return false;
        }
        if (!oldPasswordMD5.equals(EncryptUtils.encryptMD5ToString(mAccount + oldPassword))) {
            ToastUtils.show("当前密码不正确");
            mEtOldPassword.requestFocus();
            return false;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            ToastUtils.show("确认密码与新密码不一致");
            return false;
        }

        return true;
    }

    /**
     * 修改当前登录用户的密码
     */
    private void updatePassword() {
        showLoadingDialog("处理中...");

        UpdatePasswordParam parameter = new UpdatePasswordParam();
        parameter.setCurrentPassword(EncryptUtils.encryptMD5ToString(mAccount + oldPassword));
        parameter.setNewPassword(EncryptUtils.encryptMD5ToString(mAccount + newPassword));
        parameter.setConfirmNewPassword(EncryptUtils.encryptMD5ToString(mAccount + confirmNewPassword));

        String json = GsonUtils.toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);

        MDRetrofit.getInstance()
                .createService()
                .ChangeMyPassword(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String s, ErrCode errCode) {
                        dismissLoadingDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                ToastUtils.show("修改完成");
                                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 1500);

                            } else {
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

}
