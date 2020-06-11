package com.shmedo.mcloudapp.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.entity.UserInfoWrapper;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.LoginManager;
import com.shmedo.mcloudapp.util.UserConfig;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   WelcomeActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:17
 */
public class WelcomeActivity extends BaseActivity implements LoginManager.LoginCallback {

    private MaterialDialog mMaterialDialog;

    private UserConfig userConfig;

    private String mAccount = null;

    private String mPassword = null;


    @Override
    protected int initContentView() {
        return R.layout.activity_welcome;
    }


    /**
     * 沉浸式状态栏
     */
    private void initStates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStates();
        setCheckNetWork(false);
        initViewAndData();
        checkPermission();
    }

    private void initViewAndData() {
        userConfig = UserConfig.getConfig(this, CommonVariable.USER_CONFIG_NAME);
        mAccount = userConfig.readString(CommonVariable.UID);
        mPassword = userConfig.readString(CommonVariable.PWD);
    }


    private void makeAutoLogin(String account, String password) {
        if (MCloudApp.isIsNetworkConnected()) {
            LoginManager.getInstance().login(account, password, this);
        } else {
            loginForOffline();
        }
    }


    @Override
    public void callback(int code, Object data) {
        if (LoginManager.LOGIN_CODE_SUCCESS == code) {
            redirectToMainActivity(1500);

        } else {
            redirectToLoginActivity(1500);
        }
    }

    /**
     * 离线登录
     */
    private void loginForOffline() {
        UserConfig userConfig = UserConfig.getConfig(MCloudApp.getContext(), CommonVariable.USER_CONFIG_NAME);
        String account = userConfig.readString(CommonVariable.UID);
        String token = userConfig.readString(CommonVariable.ACCESS_TOKEN);
        String time = userConfig.readString(CommonVariable.TOKEN_UPDATE_TIME);

        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(token)) {
            Timber.d("token或time为空，不能离线登录");
            return;
        }

        long lastTime = Long.parseLong(time);
        long interval = new Date().getTime() - lastTime;
        if (interval / (24 * 3600 * 1000) > 28) {
            Timber.d("token超过28天有效期，不能离线登录");
            return;
        }

        DaoManager manager = DaoManager.getInstance();
        List<UserInfoWrapper> userInfoWrapperList = manager.getDaoSession().getUserInfoWrapperDao().queryBuilder().list();
        if (userInfoWrapperList != null) {
            for (UserInfoWrapper userInfoWrapper : userInfoWrapperList) {
                UserInfo userInfo = GsonFactory.getGson().fromJson(userInfoWrapper.getUserInfo(), UserInfo.class);
                if (userInfo != null && account.equals(userInfo.getUser().getAccount())) {
                    MCloudApp.setCurrentUserInfo(userInfo);
                    break;
                }
            }
        }

        MCloudApp.setAccount(account);
        MCloudApp.setAccessToken(token);

        redirectToMainActivity(1500);
    }


    /**
     * 跳转到登录界面
     */
    private void redirectToLoginActivity(long delayMillis) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LoginActivity.startActivity(WelcomeActivity.this);
                finish();
            }
        }, delayMillis);
    }

    /**
     * 跳转到主界面
     */
    private void redirectToMainActivity(long delayMillis) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NewMainActivity.start(WelcomeActivity.this);
                finish();
            }
        }, delayMillis);
    }


    private void checkPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        //自动登录
                        if (!TextUtils.isEmpty(mAccount) && !TextUtils.isEmpty(mPassword)) {
                            makeAutoLogin(mAccount, mPassword);
                        } else {
                            redirectToLoginActivity(1500);
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        if (mMaterialDialog != null && !mMaterialDialog.isShowing()) {
                            mMaterialDialog.show();

                        } else {
                            showRefusePermissionDialog();
                        }
                    }
                })
                .start();
    }


    private void showRefusePermissionDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(WelcomeActivity.this)
                .title("权限申请").content(getResources().getString(R.string.permission_external_storage))
                .negativeText("取消")
                .positiveText("去设置")
                .negativeColor(getResources().getColor(R.color.font_main))
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        XPermissionUtils.startAppSettings(WelcomeActivity.this);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == XPermissionUtils.CODE_REQUEST_PERMISSIONS) {
            checkPermission();
        }
    }

}
