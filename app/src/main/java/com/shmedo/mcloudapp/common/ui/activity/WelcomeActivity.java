package com.shmedo.mcloudapp.common.ui.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.core.util.SharedUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.model.UserInfoWrapper;
import com.shmedo.mcloudapp.common.ui.fragment.PrivacyTipDialog;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.LoginManager;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

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

    private String mAccount = null;
    private String mPassword = null;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_welcome;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStates();
        getLifecycle().addObserver(LoginManager.getInstance());
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
        mAccount = SharedUtil.read(AppContants.User.UID, "");
        mPassword = SharedUtil.read(AppContants.User.PWD, "");

        String mPrivacy = SharedUtil.read(AppContants.PRIVACY_AGREEMENT, "");
        if (!TextUtils.isEmpty(mPrivacy) && mPrivacy.equalsIgnoreCase("agree")) {
            checkLogin();
        } else {
            DialogFragment privacyTipDialog = new PrivacyTipDialog();
            privacyTipDialog.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onPositiveClick(View view) {
        /*** 友盟sdk正式初始化*/
        SharedUtil.save(AppContants.PRIVACY_AGREEMENT, "agree");
        UMConfigure.submitPolicyGrantResult(getApplicationContext(), true);
        String um_appkey;
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            um_appkey = appInfo.metaData.getString("UMENG_APP_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            um_appkey = "618cdd28e014255fcb75af8a";
            e.printStackTrace();
        }
        UMConfigure.init(this, um_appkey, "production", UMConfigure.DEVICE_TYPE_PHONE, "");

        checkLogin();
    }

    @Override
    public void onNegativeClick(View view) {
        //不同意隐私协议，退出app
        UMConfigure.submitPolicyGrantResult(getApplicationContext(), false);
//        WelcomeActivity.this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void checkLogin() {
        //自动登录
        if (!TextUtils.isEmpty(mAccount) && !TextUtils.isEmpty(mPassword)) {
            //当用户使用自有账号登录时，可以这样统计：
            MobclickAgent.onProfileSignIn(mAccount);
            makeAutoLogin(mAccount, mPassword);
        } else {
            redirectToLoginActivity(1000);
        }
    }

    private void makeAutoLogin(String account, String password) {
        LoginManager.getInstance().login(account, password, this);
//        if (MCloudApp.isIsNetworkConnected()) {
//            LoginManager.getInstance().login(account, password, this);
//        } else {
//            loginForOffline();
//        }
    }

    /**
     * 离线登录
     */
    private void loginForOffline() {
        String account = SharedUtil.read(AppContants.User.UID);
        String token = SharedUtil.read(NetworkConst.ACCESS_TOKEN);
        String time = SharedUtil.read(AppContants.TOKEN_UPDATE_TIME);

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
                MainActivity.start(WelcomeActivity.this);
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