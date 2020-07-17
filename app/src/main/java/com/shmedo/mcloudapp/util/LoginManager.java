package com.shmedo.mcloudapp.util;

import android.os.Handler;

import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.SharedUtil;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.entity.UserInfoWrapper;
import com.shmedo.mcloudapp.entity.parameter.SignInParameter;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;

import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   gonghe
 * 创建时间:  2019-10-17
 */
public class LoginManager {

    public static final int LOGIN_CODE_SUCCESS = 0;

    public static final int LOGIN_CODE_FAIL_EXCEPTION = 0x0040;

    public static final int LOGIN_CODE_FAIL_BUSINESS = 0x0041;

    private static LoginManager instance = new LoginManager();

    private Handler handler = null;

    private LoginCallback loginCallback = null;

    private String mAccount = null;

    private String mPassword = null;

    private String Code = null;

    private String Mobile = null;


    public static LoginManager getInstance() {
        return instance;
    }

    private LoginManager() {

    }


    public void login(String account, String password, final LoginCallback callback) {
        this.loginCallback = callback;
        this.mAccount = account;
        this.mPassword = password;

        makeLoginByAccount();
    }

    public void quickLogin(String mobile, String code, final LoginCallback callback) {
        this.loginCallback = callback;
        this.Mobile = mobile;
        this.Code = code;
        makeQuickLogin();
    }

    /**
     * 账户密码登录
     */
    private void makeLoginByAccount() {
        SignInParameter parameter = new SignInParameter(mAccount, mPassword);
        parameter.setPassword(MD5Util.MD5(mAccount + mPassword));
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);

        MDRetrofit.getInstance().createService()
                .getSingIn(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {

                    @Override
                    public void Success(String token, String message) {
                        getUserInfo(token);
                    }

                    @Override
                    public void Failure(String message) {
                        if (loginCallback != null) {
                            loginCallback.callback(LOGIN_CODE_FAIL_EXCEPTION, message);
                        }
                    }
                });
    }

    /**
     * 手机验证码登录
     */
    private void makeQuickLogin() {
        SignInParameter parameter = new SignInParameter(Mobile, Code);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);

        MDRetrofit.getInstance().createService().SmsLogin(body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new BaseObserver<String>() {
            @Override
            public void Success(String token, String message) {
                if (token.contains("手机号对应的用户不存在")) {
                    ToastUtils.show("手机号对应的用户不存在");
                    //TODO 手机号不存在设置为游客登录
                    if (loginCallback != null) {
                        loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, message);
                    }
                } else {
                    getUserInfo(token);
                }
            }

            @Override
            public void Failure(String message) {
                if (loginCallback != null) {
                    loginCallback.callback(LOGIN_CODE_FAIL_EXCEPTION, message);
                }
            }
        });
    }


    /**
     * 获取用户信息
     *
     * @param token
     */
    public void getUserInfo(final String token) {
        MDRetrofit.getInstance().createService().getMyInfo(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<UserInfo>() {
                    @Override
                    public void Success(UserInfo userInfo, String message) {
                        //在内存中保存用户数据为全局变量
                        MCloudApp.setAccessToken(token);
                        MCloudApp.setAccount(userInfo.getUser().getAccount());
                        MCloudApp.setCurrentUserInfo(userInfo);

                        //下面是持久化保存用户数据
                        Long id = (long) userInfo.getUser().getId();
                        UserInfoWrapper userInfoWrapper = new UserInfoWrapper();
                        userInfoWrapper.setId(id);
                        userInfoWrapper.setUserInfo(GsonFactory.getGson().toJson(userInfo));
                        DaoManager manager = DaoManager.getInstance();
                        manager.getDaoSession().getUserInfoWrapperDao().insertOrReplace(userInfoWrapper);

                        SharedUtil.save(NetworkConst.ACCESS_TOKEN, token);
                        SharedUtil.save(AppContants.TOKEN_UPDATE_TIME, new Date().getTime() + "");
                        if (mAccount != null && mPassword != null) {
                            SharedUtil.save(AppContants.User.UID, mAccount);
                            SharedUtil.save(AppContants.User.PWD, mPassword);
                        }

                        if (loginCallback != null) {
                            loginCallback.callback(LOGIN_CODE_SUCCESS, "登录成功");
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        if (loginCallback != null) {
                            loginCallback.callback(LOGIN_CODE_FAIL_EXCEPTION, message);
                        }
                    }
                });
    }


    public interface LoginCallback {
        void callback(int code, Object data);
    }
}
