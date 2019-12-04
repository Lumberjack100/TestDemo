package com.shmedo.mcloudapp.util;

import android.os.Handler;

import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.entity.UserInfoWrapper;
import com.shmedo.mcloudapp.entity.parameter.SignInParameter;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   gonghe
 * 创建时间:  2019-10-17
 * 描述：    TODO
 */
public class LoginManager {

    public static final int LOGIN_CODE_SUCCESS = 0;

    public static final int LOGIN_CODE_FAIL_EXCEPTION = 0x0040;

    public static final int LOGIN_CODE_FAIL_BUSINESS = 0x0041;

    public static final int USER_INFO_CODE_SUCCESS = 0;

    public static final int USER_INFO_CODE_FAIL = -1;

    private static LoginManager instance = new LoginManager();

    private Handler handler = null;

    private LoginCallback loginCallback = null;

    private String mAccount = null;

    private String mPassword = null;


    public static LoginManager getInstance() {
        return instance;
    }

    private LoginManager() {

    }


    public void login(String account, String password, final LoginCallback callback) {
        this.loginCallback = callback;
        this.mAccount = account;
        this.mPassword = password;

        makeLogin();
    }


    private void makeLogin() {
        SignInParameter parameter = new SignInParameter(mAccount, mPassword);
        parameter.setPassword(MD5Util.MD5(mAccount + mPassword));
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);

        MDRetrofit.getInstance().createService().getSingIn(body)
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

                    public void onError(Throwable e) {
                        super.onError(e);
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
                        MCloudApp.setAccessToken(token);
                        MCloudApp.setAccount(userInfo.getUser().getAccount());
                        MCloudApp.setCurrentUserInfo(userInfo);

                        Long id = Long.valueOf(userInfo.getUser().getId());
                        UserInfoWrapper userInfoWrapper = new UserInfoWrapper();
                        userInfoWrapper.setId(id);
                        userInfoWrapper.setUserInfo(GsonFactory.getGson().toJson(userInfo));

                        DaoManager manager = DaoManager.getInstance();
                        manager.getDaoSession().getUserInfoWrapperDao().insertOrReplace(userInfoWrapper);

                        UserConfig userConfig = UserConfig.getConfig(MCloudApp.getContext(), CommonVariable.USER_CONFIG_NAME);
                        if (mAccount != null && mPassword != null) {
                            userConfig.writeString(CommonVariable.UID, mAccount);
                            userConfig.writeString(CommonVariable.PWD, mPassword);
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
