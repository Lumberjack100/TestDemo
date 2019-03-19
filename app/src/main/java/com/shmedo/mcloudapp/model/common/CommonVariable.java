package com.shmedo.mcloudapp.model.common;

import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.util.StringUtil;
import okhttp3.MediaType;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model.common
 * 文件名:   CommonVariable
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:34
 * 描述：    TODO
 */
public class CommonVariable {

    private static String serviceAddress;
    public static final String  USER_CONFIG_NAME="mcloudApp";
    public static final String SERVICE_ADDRESS="service_address";
    public static final String  ACCESS_TOKEN="access_token";
    public static final String APP_KEY = "b4524704-b325-4c88-a0dc-bfce89d58138";
    public static final String APP_SECRET = "c1507673-7a16-4cba-817c-2595a9c8a6e8";
    public static final String UID="uid";
    public static final String PWD="pwd";
    public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private static UserInfo currentUserInfo;
    public static final String ACCOUNT="account";
    public static final String USER_ID="userID";
    public static final String HEAD_PHOTO_PATH="headPhotoPath";
    private static String accessToken;
    private static String account;
    private static String password;
    private static boolean isNetworkConnected=true;

    public static String getServiceAddress() {
        if(StringUtil.isNullOrEmpty(serviceAddress))
            return serviceAddress;
        return "http://"+serviceAddress+"/api/v1/";
    }


    public static void setServiceAddress(String serviceAddress) {
        CommonVariable.serviceAddress = serviceAddress;
    }


    public static UserInfo getCurrentUserInfo() {
        return currentUserInfo;
    }


    public static void setCurrentUserInfo(UserInfo currentUserInfo) {
        CommonVariable.currentUserInfo = currentUserInfo;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        CommonVariable.accessToken = accessToken;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        CommonVariable.account = account;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        CommonVariable.password = password;
    }

    public static boolean isNetworkConnected() {
        return isNetworkConnected;
    }

    public static void setIsNetworkConnected(boolean isNetworkConnected) {
        CommonVariable.isNetworkConnected = isNetworkConnected;
    }
}
