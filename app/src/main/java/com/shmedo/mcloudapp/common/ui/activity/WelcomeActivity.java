package com.shmedo.mcloudapp.common.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.MetaDataUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.model.UserInfoWrapper;
import com.shmedo.mcloudapp.common.ui.fragment.PolicyDialog;
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
public class WelcomeActivity extends BaseActivity implements LoginManager.LoginCallback, PolicyDialog.PolicyClickListener {

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
        String mPrivacy = SPStaticUtils.getString(AppContants.PRIVACY_AGREEMENT, "");
        if (!TextUtils.isEmpty(mPrivacy) && mPrivacy.equalsIgnoreCase("agree")) {
            goToLogin();
        } else {
            DialogFragment privacyTipDialog = new PolicyDialog();
            privacyTipDialog.show(getSupportFragmentManager(), "dialog");
        }
    }

    /**
     * 同意协议
     *
     * @param view
     */
    @Override
    public void onAgreeClick(View view) {
        SPStaticUtils.put(AppContants.PRIVACY_AGREEMENT, "agree");
        /*** 友盟sdk正式初始化*/
        UMConfigure.submitPolicyGrantResult(getApplicationContext(), true);
        String appKey = MetaDataUtils.getMetaDataInApp("UMENG_APP_KEY");
        UMConfigure.init(this, appKey, "production", UMConfigure.DEVICE_TYPE_PHONE, "");

        goToLogin();
    }

    /**
     * 不同意协议
     *
     * @param view
     */
    @Override
    public void onDisagreeClick(View view) {
        //不同意隐私协议，退出app
        UMConfigure.submitPolicyGrantResult(getApplicationContext(), false);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void goToLogin() {
        String mAccount = SPStaticUtils.getString(AppContants.User.UID, "");
        String mPassword = SPStaticUtils.getString(AppContants.User.PWD, "");
        String Md5Password = SPStaticUtils.getString(AppContants.User.MD5_PWD, "");
        //自动登录
        if (!TextUtils.isEmpty(mAccount) && (!TextUtils.isEmpty(mPassword) | !TextUtils.isEmpty(Md5Password))) {
            //当用户使用自有账号登录时，可以这样统计：
            MobclickAgent.onProfileSignIn(mAccount);

            String pwd = !TextUtils.isEmpty(Md5Password) ? Md5Password : EncryptUtils.encryptMD5ToString(mAccount + mPassword);
            LoginManager.getInstance().login(mAccount, pwd, this);
        } else {
            redirectToLoginActivity(1000);
        }
    }

    private void makeAutoLogin(String account, String password) {
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
        String account = SPStaticUtils.getString(AppContants.User.UID);
        String token = SPStaticUtils.getString(NetworkConst.ACCESS_TOKEN);
        String time = SPStaticUtils.getString(AppContants.TOKEN_UPDATE_TIME);
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
                UserInfo userInfo = GsonUtils.fromJson(userInfoWrapper.getUserInfo(), UserInfo.class);
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
            goToLogin();
        }
    }

}