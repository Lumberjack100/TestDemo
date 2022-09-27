package com.shmedo.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.shmedo.core.model.UserWrapperInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 创建者:   gonghe
 * 创建时间:  2019-10-21
 * 描述：   全局的API接口
 */
public class MCloudApp {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    private static Handler handler;

    private static UserWrapperInfo currentUserWrapperInfo;
    private static String accessToken;
    private static int userID;
    private static int companyID;
    private static int productID;
    private static String curDeviceToken;//设备名称
    private static List<String> permissionTokenList = new ArrayList<>();//用户在某公司某服务中的所有权限名称

    private static List<Integer> companyIdList = new ArrayList<>();//查询用户所在的所有公司Id列表


    /**
     * 初始化接口。这里会进行应用程序的初始化操作，一定要在代码执行的最开始调用。
     *
     * @param c Context参数，注意这里要传入的是Application的Context，千万不能传入Activity或者Service的Context。
     */
    public static void initialize(Context c) {
        mContext = c;
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 获取全局Context，在代码的任意位置都可以调用，随时都能获取到全局Context对象。
     *
     * @return 全局Context对象。
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * 获取创建在主线程上的Handler对象。
     *
     * @return 创建在主线程上的Handler对象。
     */
    public static Handler getMainHandler() {
        return handler;
    }

    public static UserWrapperInfo getCurrentUserInfo() {
        return currentUserWrapperInfo;
    }

    public static void setCurrentUserInfo(UserWrapperInfo currentUserWrapperInfo) {
        MCloudApp.currentUserWrapperInfo = currentUserWrapperInfo;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        MCloudApp.accessToken = accessToken;
    }

    public static int getCompanyID() {
        return companyID;
    }

    public static void setCompanyID(int companyID) {
        MCloudApp.companyID = companyID;
    }

    public static int getUserID() {
        return userID;
    }

    public static void setUserID(int userID) {
        MCloudApp.userID = userID;
    }

    public static String getCurDeviceToken() {
        return curDeviceToken;
    }

    public static void setCurDeviceToken(String curDeviceToken) {
        MCloudApp.curDeviceToken = curDeviceToken;
    }

    public static int getProductID() {
        return productID;
    }

    public static void setProductID(int productID) {
        MCloudApp.productID = productID;
    }

    public static List<String> getPermissionTokenList() {
        return permissionTokenList;
    }

    public static void setPermissionTokenList(List<String> permissionTokenList) {
        if (permissionTokenList != null) {
            MCloudApp.permissionTokenList.clear();
            MCloudApp.permissionTokenList.addAll(permissionTokenList);
        }
    }

    public static List<Integer> getCompanyIdList() {
        return companyIdList;
    }

    public static void setCompanyIdList(List<Integer> companyIdList) {
        if (companyIdList != null) {
            MCloudApp.companyIdList.clear();
            MCloudApp.companyIdList.addAll(companyIdList);
        }
    }

    /**
     * 注销用户登录。
     */
    public static void logout() {
        accessToken = null;
        currentUserWrapperInfo = null;
        SPStaticUtils.remove(AppContants.User.PWD);
        ActivityUtils.finishAllActivities();
    }
}
