package com.shmedo.mcloudapp.util;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.SPStaticUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.BasicUserInfo;
import com.shmedo.core.model.UserPermissionInfo;
import com.shmedo.core.model.UserWrapperInfo;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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


    public static LoginManager getInstance() {
        return instance;
    }

    private LoginManager() {

    }

    public void login(final String account, final String password, final LoginCallback callback) {
        this.loginCallback = callback;
        makeLoginByAccount(account, password);
    }

    public void quickLogin(final String mobile, final String code, final LoginCallback callback) {
        this.loginCallback = callback;
        makeQuickLogin(mobile, code);
    }

    /**
     * 账户密码登录
     */
    private void makeLoginByAccount(final String account, final String password) {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("account", account);
            jsonObjectRequest.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService()
                .getSingIn(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String token, ErrorInfo errorInfo) {
                        if (errorInfo.getCode() == 0) {
                            //在内存中保存用户数据为全局变量
                            MCloudApp.setAccessToken(token);
                            //持久化保存用户数据到SharedPreferences文件中
                            SPStaticUtils.put(AppContants.User.UID, account);
                            SPStaticUtils.put(AppContants.User.PWD, password);
                            getUserByToken();
                        } else {
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, errorInfo.getMsg());
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
    private void makeQuickLogin(final String mobile, final String code) {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("phone", mobile);
            jsonObjectRequest.put("code", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService()
                .smsLogin(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String token, ErrorInfo errorInfo) {
                        if (errorInfo.getCode() == 0) {
                            //在内存中保存用户数据为全局变量
                            MCloudApp.setAccessToken(token);
                            getUserByToken();
                        } else {
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, errorInfo.getMsg());
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
     * 通过token获取用户信息
     */
    private void getUserByToken() {
        MDRetrofit.getInstance()
                .createService()
                .getUserByToken(MCloudApp.getAccessToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BasicUserInfo>() {
                    @Override
                    protected void onResponse(BasicUserInfo basicUserInfo, ErrorInfo errorInfo) {
                        if (errorInfo.getCode() == 0) {
                            MCloudApp.setCompanyID(basicUserInfo.getCompanyID());
                            MCloudApp.setUserID(basicUserInfo.getSubjectID());
                            queryAllPermissionInService(basicUserInfo.getCompanyID(), basicUserInfo.getSubjectID());
                        } else {
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, errorInfo.getMsg());
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
     * 查询用户在某公司某服务中的所有权限
     */
    private void queryAllPermissionInService(final int companyID, final int userID) {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", companyID);
            jsonObjectRequest.put("userID", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService()
                .queryAllPermissionInService(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<UserPermissionInfo>>() {
                    @Override
                    protected void onResponse(List<UserPermissionInfo> permissionInfoList, ErrorInfo errorInfo) {
                        if (errorInfo.getCode() == 0) {
                            if (!checkPermission(permissionInfoList))
                                return;

                            //用户在某公司某服务中的所有权限
                            MCloudApp.setUserPermissionInfoList(permissionInfoList);
                            queryUserByID(companyID, userID);
                        } else {
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, errorInfo.getMsg());
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

    private boolean checkPermission(List<UserPermissionInfo> permissionInfoList) {
        if (permissionInfoList == null || permissionInfoList.isEmpty()) {
            if (loginCallback != null) {
                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, "缺少权限");
            }
            return false;
        }

        List<String> tempList = new ArrayList<>();
        for (UserPermissionInfo permissionInfo : permissionInfoList) {
            tempList.add(permissionInfo.getPermissionToken());
        }
        if (!tempList.contains("DescribeUser")) {
            if (loginCallback != null) {
                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, "缺少查询用户(DescribeUser)权限");
            }
            return false;
        }
        if (!tempList.contains("DescribeCompany")) {
            if (loginCallback != null) {
                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, "缺少查询公司(DescribeCompany)权限");
            }
            return false;
        }
        if (!tempList.contains("DescribeIotDashboard")) {
            if (loginCallback != null) {
                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, "缺少统计公司下的设备(DescribeIotDashboard)权限");
            }
            return false;
        }
        if (!tempList.contains("ListDevice")) {
            if (loginCallback != null) {
                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, "缺少查询设备分页列表(ListDevice)权限");
            }
            return false;
        }
        if (!tempList.contains("DescribeDevice")) {
            if (loginCallback != null) {
                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, "缺少描述设备(DescribeDevice)权限");
            }
            return false;
        }
        return true;
    }

    /**
     * 查询用户信息
     */
    private void queryUserByID(final int companyID, final int userID) {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", companyID);
            jsonObjectRequest.put("userID", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService()
                .queryUserByID(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<UserWrapperInfo>() {
                    @Override
                    protected void onResponse(UserWrapperInfo userWrapperInfo, ErrorInfo errorInfo) {
                        if (errorInfo.getCode() == 0) {
                            //在内存中保存用户数据为全局变量
                            if (userWrapperInfo.getUser() != null) {
                                MCloudApp.setCurrentUserInfo(userWrapperInfo);
                            }
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_SUCCESS, "登录成功");
                            }
                        } else {
                            if (loginCallback != null) {
                                loginCallback.callback(LOGIN_CODE_FAIL_BUSINESS, errorInfo.getMsg());
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
        void callback(int code, String msg);
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        loginCallback = null;
    }
}
