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

import com.blankj.utilcode.util.DebouncingUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.ui.activity.LoginActivity;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.util.ResponseHandler;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

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

    @BindView(R.id.newPasswordET)
    ClearEditText mEtNewPassword;

    @BindView(R.id.confirmNewPasswordET)
    ClearEditText mEtConfirmPassword;

    @BindView(R.id.iv_eye_new_password)
    ImageView mIvEyeNewPwd;

    @BindView(R.id.iv_eye_confirm_password)
    ImageView mIvEyeConfirmPwd;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private String newPassword, confirmNewPassword;

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
    }

    @OnClick({R.id.iv_eye_new_password, R.id.iv_eye_confirm_password, R.id.btn_confirm})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.iv_eye_new_password) {//
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
        } else if (id == R.id.iv_eye_confirm_password) {//
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
        } else if (id == R.id.btn_confirm) {//
            if (checkValue()) {
                updatePassword();
            }
        }
    }

    private boolean checkValue() {
        newPassword = mEtNewPassword.getText().toString();
        confirmNewPassword = mEtConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(newPassword)) {
            ToastUtils.show("请输入新密码");
            mEtNewPassword.requestFocus();
            return false;
        }
        if (!ValidateUtil.checkPassword(newPassword)) {
            ToastUtils.show("密码格式错误！");
            mEtNewPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(confirmNewPassword)) {
            ToastUtils.show("请输入确认密码");
            mEtConfirmPassword.requestFocus();
            return false;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            ToastUtils.show("两次密码不一致");
            return false;
        }
        return true;
    }

    /**
     * 修改当前登录用户的密码
     */
    private void updatePassword() {
        showWaitDialog("处理中...");
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", MCloudApp.getCompanyID());
            jsonObjectRequest.put("userID", MCloudApp.getUserID());
            jsonObjectRequest.put("newPassword", newPassword);
            jsonObjectRequest.put("confirmPassword", confirmNewPassword);
        } catch (JSONException e) {
            dismissWaitDialog();
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService()
                .resetPassword(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String s, ErrorInfo errorInfo) {
                        dismissWaitDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                ToastUtils.show("已修改");
                                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        MCloudApp.logout();
                                        //登出
                                        MobclickAgent.onProfileSignOff();
                                        LoginActivity.startActivity(UpdatePasswordActivity.this);
                                    }
                                }, 1000);
                            } else {
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissWaitDialog();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

}
