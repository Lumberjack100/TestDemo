package com.shmedo.lib.cmd.base.iot_cmd.enums

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/9/26 <br></br>
 * 描述：    数据中心平台枚举
 */
enum class DataCenterPlatform(private val platName: String, private val cmdValue: String) {
    DIDA_PLATFORM("地大平台", "0"),
    CHINA_MOBILE_PLATFORM("中移物联平台", "1"),
    MEDO_IOT_PLATFORM("米度物联平台", "2"),
    DIDA2_PLATFORM("地大平台2", "3"),
    HENAN_WATER_PLATFORM("河南水文平台", "4"),
    MEDO_WATER_PLATFORM("米度水文平台", "5"),
    AMS_PLATFORM("米度解算平台", "6"),
    CHONGQING_DISASTER_PLATFORM("重庆地灾平台", "7"),
    GUANGDONG_WATER_PLATFORM("广东水文平台", "8"),
    GUANGXI_WATER_PLATFORM("广西水文平台", "9"),
    HUBEI_WATER_PLATFORM("湖北水文平台", "10"),
    HUBEI_ECO_PLATFORM("湖北生态流量平台", "11"),
    GUIZHOU_ENCRYPT_PLATFORM("贵州加密平台", "12"),
    CORS_PLATFORM("CORS平台", "13"),
    BEIJING_LUAN_PLATFORM("北京路安平台", "14"),
    GUANGDONG_FLOOD_PLATFORM("广东山洪预警平台", "15")

    ;

    fun getPlatName(): String {
        return platName
    }

    fun getCmdValue(): String {
        return cmdValue
    }


    companion object {
        @JvmStatic
        fun valueByPlatformName(name: String): DataCenterPlatform {
            if (TextUtils.isEmpty(name)) return MEDO_IOT_PLATFORM
            for (platform in entries) {
                if (platform.platName == name) return platform

            }
            return MEDO_IOT_PLATFORM
        }

        @JvmStatic
        fun valueByCmdValue(cmdValue: String): DataCenterPlatform {
            if (TextUtils.isEmpty(cmdValue)) return MEDO_IOT_PLATFORM
            for (platform in entries) {
                if (platform.cmdValue == cmdValue) return platform
            }
            return MEDO_IOT_PLATFORM
        }

        @JvmStatic
        val platNames: List<String>
            get() = entries.map { it.platName }

    }
}