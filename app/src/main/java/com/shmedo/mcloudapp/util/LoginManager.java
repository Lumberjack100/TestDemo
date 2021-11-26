package com.shmedo.mcloudapp.util;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.common.model.params.SignInParameter;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   gonghe
 * 创建时间:  2019-10-17
 */
public class LoginManager implements DefaultLifecycleObserver {

    public static final int LOGIN_CODE_SUCCESS = 0;

    public static final int LOGIN_CODE_FAIL_BUSINESS = 0x0040;

    public static final int LOGIN_CODE_FAIL_EXCEPTION = 0x0041;

    private static LoginManager instance = new LoginManager();

    private LoginCallback loginCallback = null;

    private String Code = null;

    private String Mobile = null;


    public static LoginManager getInstance() {
        return instance;
    }

    private LoginManager() {

    }

    public void login(String account, String password, final LoginCallback callback) {
        this.loginCallback = callback;
        makeLoginByAccount(account, password);
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
    private void makeLoginByAccount(String account, String password) {
        SignInParameter parameter = new SignInParameter(account, password);
        String json = GsonUtils.toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);

        MDRetrofit.getInstance().createService()
                .getSingIn(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String token, ErrCode errCode) {
                        if (errCode.getCode() == 0) {
                            getUserInfo(token);
                        } else {
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, errCode.getErrMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                        if (loginCallback != null) {
                            loginCallback.callback(LOGIN_CODE_FAIL_EXCEPTION, e.getMessage());
                        }
                    }
                });
    }

    /**
     * 手机验证码登录
     */
    private void makeQuickLogin() {
        SignInParameter parameter = new SignInParameter(Mobile, Code);
        String json = GsonUtils.toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .SmsLogin(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String token, ErrCode errCode) {
                        if (errCode.getCode() == 0) {
                            if (token.contains("手机号对应的用户不存在")) {
                                ToastUtils.show("手机号对应的用户不存在");
                                if (loginCallback != null) {
                                    loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, null);
                                }
                            } else {
                                getUserInfo(token);
                            }
                        } else {
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, errCode.getErrMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                        if (loginCallback != null) {
                            loginCallback.callback(LOGIN_CODE_FAIL_EXCEPTION, e.getMessage());
                        }
                    }
                });
    }


    /**
     * 获取用户信息
     *
     * @param token
     */
    private void getUserInfo(final String token) {
        MDRetrofit.getInstance().createService().getMyInfo(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<UserInfo>() {
                    @Override
                    protected void onResponse(UserInfo userInfo, ErrCode errCode) {
                        if (errCode.getCode() == 0) {
                            //在内存中保存用户数据为全局变量
                            MCloudApp.setAccessToken(token);
                            if (userInfo.getUser() != null) {
                                MCloudApp.setAccount(userInfo.getUser().getAccount());
                                MCloudApp.setCurrentUserInfo(userInfo);
                                if (userInfo.getDepartments() != null && userInfo.getDepartments().size() > 0) {
                                    int companyID = userInfo.getDepartments().get(0).getCompanyID();
                                    MCloudApp.setCompanyID(companyID);
                                }
                                //持久化保存用户数据到SharedPreferences文件中
                                if (!TextUtils.isEmpty(userInfo.getUser().getPassword())) {
                                    SPStaticUtils.put(AppContants.User.UID, userInfo.getUser().getAccount());
                                    SPStaticUtils.put(AppContants.User.MD5_PWD, userInfo.getUser().getPassword());
                                    SPStaticUtils.remove(AppContants.User.PWD);
                                }
//                                SPStaticUtils.put(NetworkConst.ACCESS_TOKEN, token);
                            }
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_SUCCESS, "登录成功");
                            }
                        } else {
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, errCode.getErrMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                        if (loginCallback != null) {
                            loginCallback.callback(LOGIN_CODE_FAIL_EXCEPTION, e.getMessage());
                        }
                    }
                });
    }

    public interface LoginCallback {
        void callback(int code, Object data);
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        loginCallback = null;
    }
}
