package com.shmedo.mcloudapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.shmedo.mcloudapp.AppContants;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.entity.UserInfoWrapper;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.ui.PrivacyTipDialog;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.LoginManager;
import com.shmedo.mcloudapp.util.UserConfig;
import com.shmedo.mcloudapp.util.XPermissionUtils;

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
public class WelcomeActivity extends BaseActivity implements LoginManager.LoginCallback, PrivacyTipDialog.DialogFragmentClickListener {
    private UserConfig userConfig;

    private String mAccount = null;
    private String mPassword = null;


    @Override
    protected int initContentView() {
        return R.layout.activity_welcome;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStates();
        setCheckNetWork(false);
        initData();
    }

    /**
     * 沉浸式状态栏
     */
    private void initStates() {
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    private void initData() {
        userConfig = UserConfig.getConfig(this,  AppContants.APP_CONFIG_NAME);
        mAccount = userConfig.readString(AppContants.User.UID);
        mPassword = userConfig.readString(AppContants.User.PWD);

        String mPrivacy = userConfig.readString(AppContants.PRIVACY_AGREEMENT);
        if (TextUtils.isEmpty(mPrivacy) || mPrivacy.toLowerCase().equals("refuse")) {
            DialogFragment privacyTipDialog = new PrivacyTipDialog();
            privacyTipDialog.show(getSupportFragmentManager(), "dialog");
        } else {
            checkLogin();
        }
    }

    @Override
    public void onPositiveClick(View view) {
        userConfig.writeString(AppContants.PRIVACY_AGREEMENT, "agree");
        checkLogin();
    }

    @Override
    public void onNegativeClick(View view) {
        userConfig.writeString(AppContants.PRIVACY_AGREEMENT, "refuse");
        WelcomeActivity.this.finish();
    }

    private void checkLogin() {
        //自动登录
        if (!TextUtils.isEmpty(mAccount) && !TextUtils.isEmpty(mPassword)) {
            makeAutoLogin(mAccount, mPassword);
        } else {
            redirectToLoginActivity(1000);
        }
    }

    private void makeAutoLogin(String account, String password) {
        if (MCloudApp.isIsNetworkConnected()) {
            LoginManager.getInstance().login(account, password, this);
        } else {
            loginForOffline();
        }
    }

    /**
     * 离线登录
     */
    private void loginForOffline() {
        String account = userConfig.readString(AppContants.User.UID);
        String token = userConfig.readString(NetworkConst.ACCESS_TOKEN);
        String time = userConfig.readString(AppContants.TOKEN_UPDATE_TIME);

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

    @Override
    public void callback(int code, Object data) {
        if (LoginManager.LOGIN_CODE_SUCCESS == code) {
            redirectToMainActivity(1500);

        } else {
            redirectToLoginActivity(1500);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == XPermissionUtils.REQUEST_CODE_OPEN_APPLICATION_SETTING) {
            checkLogin();
        }
    }

}