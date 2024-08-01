package com.shmedo.lib.network.util

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/5/26 <br></br>
 * 描述：   访问的后台地址切换
 */
enum class BaseURL(@JvmField var baseUrl: String) {
    /**
     * 物联网权限服务地址
     */
    AUTHORITY_SERVICE_ADDRESS("https://opengw.shmedo.cn/base/auth/api/v1/"),

    /**
     * 物联网设备管理服务地址
     */
    IOT_MANAGER_SERVICE_ADDRESS("https://opengw.shmedo.cn/iot/manager/api/v1/"),

    /**
     * 物联网指令交互服务地址
     */
    IOT_INTERACTIVE_SERVICE_ADDRESS("https://opengw.shmedo.cn/iot/interactive/api/v1/"),

    /**
     * 云平台原始数据地址
     */
    CLOUD_PLATFORM_DATA_ADDRESS("https://queryapi.shmedo.cn/"),

    /**
     * 孙建伟通用服务地址
     */
    AMS_CONFIG_ADDRESS("http://ams4.shmedo.com:22000/api/v1/"),

    /**
     * 蒲公英服务地址
     */
    PGYER_SERVICE_ADDRESS("https://www.pgyer.com/apiv2/app/"),


    /**
     * 自定义 Url
     */
    CUSTOM_ADDRESS("");

    override fun toString(): String {
        return baseUrl
    }

    companion object {
        fun getCustomAddress(baseUrl: String): BaseURL? {
            if (!TextUtils.isEmpty(baseUrl)) {
                CUSTOM_ADDRESS.baseUrl = baseUrl
                return CUSTOM_ADDRESS
            }
            return null
        }
    }
}
