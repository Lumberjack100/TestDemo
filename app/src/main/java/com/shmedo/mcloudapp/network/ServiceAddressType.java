package com.shmedo.mcloudapp.network;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/5/26 <br/>
 * 描述：   访问的后台地址切换
 */
public enum ServiceAddressType {
    /**
     * 物联网权限服务地址
     */
    AUTHORITY_SERVICE_ADDRESS("https://mdiotbff.shmedo.cn/api/v1/auth/"),

    /**
     * 物联网设备管理服务地址
     */
    IOT_MANAGER_SERVICE_ADDRESS("https://mdiotbff.shmedo.cn/api/v1/iot/"),

    /**
     * 物联网指令交互服务地址
     */
    IOT_INTERACTIVE_SERVICE_ADDRESS("https://mdiotbff.shmedo.cn/api/v1/interactive/"),

    /**
     * 云平台原始数据地址
     */
    CLOUD_PLATFORM_DATA_ADDRESS("https://chaxun.shmedo.cn"),

    /**
     * 设备远程调试服务器地址
     */
//    DEVICE_REMOTE_DEBUG_ADDRESS("http://ams4.shmedo.com:22000/api/v1/"),

    /**
     * 自定义 Url
     */
    CUSTOM_ADDRESS("");


    private String baseUrl;

    ServiceAddressType(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }


    @Override
    public String toString() {
        return baseUrl;
    }

    public static ServiceAddressType getCustomAddress(String baseUrl) {
        if (!TextUtils.isEmpty(baseUrl)) {
            CUSTOM_ADDRESS.baseUrl = baseUrl;
            return CUSTOM_ADDRESS;
        }
        return null;
    }
}
