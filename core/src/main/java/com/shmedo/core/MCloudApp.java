package com.shmedo.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.shmedo.core.model.UserInfo;


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

    private static UserInfo currentUserInfo;
    private static String accessToken;
    private static String account;
    private static int companyID;
    private static String curDeviceToken;//设备名称

    private static final String businessServiceAddress = "mdnetservice.shmedo.cn";//业务数据服务接口地址
    private static final String authorityServiceAddress = "http://172.168.5.200:10082";//物联网权限服务地址
    private static final String iotManagerServiceAddress = "http://172.168.5.200:10081";//物联网设备管理服务地址
    private static final String iotInteractiveServiceAddress = "http://172.168.5.200:10083";//物联网指令交互服务地址
    private static final String cloudPlatformDataAddress = "https://chaxun.shmedo.cn";//云平台原始数据地址


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

    public static String getBusinessServiceAddress() {
        return "https://" + businessServiceAddress + "/api/v1/";
    }

    public static String getAuthorityServiceAddress() {
        return authorityServiceAddress + "/auth/api/v1/";
    }

    public static String getIotManagerServiceAddress() {
        return iotManagerServiceAddress + "/iot/manager/api/v1/";
    }

    public static String getIotInteractiveServiceAddress() {
        return iotInteractiveServiceAddress + "/iot/interactive/api/v1/";
    }

    public static String getCloudPlatformDataAddress() {
        return cloudPlatformDataAddress;
    }

    public static UserInfo getCurrentUserInfo() {
        return currentUserInfo;
    }

    public static void setCurrentUserInfo(UserInfo currentUserInfo) {
        MCloudApp.currentUserInfo = currentUserInfo;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        MCloudApp.accessToken = accessToken;
    }


    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        MCloudApp.account = account;
    }


    public static int getCompanyID() {
        return companyID;
    }

    public static void setCompanyID(int companyID) {
        MCloudApp.companyID = companyID;
    }


    public static String getCurDeviceToken() {
        return curDeviceToken;
    }

    public static void setCurDeviceToken(String curDeviceToken) {
        MCloudApp.curDeviceToken = curDeviceToken;
    }

    /**
     * 注销用户登录。
     */
    public static void logout() {
        accessToken = null;
        account = null;
        currentUserInfo = null;
        SPStaticUtils.remove(AppContants.User.PWD);
        SPStaticUtils.remove(AppContants.User.MD5_PWD);
        ActivityUtils.finishAllActivities();
    }
}
