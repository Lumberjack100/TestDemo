package com.shmedo.mcloudapp.model

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/11/23 <br></br>
 * 描述：   设备状态信息
 */
enum class DeviceStatusEnum(val code: String, val description: String) {
    NORMAL("0", "正常"),
    DEVICE_WARN("-2", "设备告警"),
    DEVICE_ERROR("-3", "设备故障"),

    UNKNOWN("-200", "未知");

    companion object {
        @JvmStatic
        fun valueByCode(code: String): DeviceStatusEnum {
            if (TextUtils.isEmpty(code)) return NORMAL
            for (errorType in entries) {
                if (errorType.code == code) return errorType
            }
            return UNKNOWN
        }
    }
}