package com.shmedo.lib.cmd.base.iot_cmd.enums

import android.text.TextUtils

/**
 * 创建者：gonghe
 * 创建时间：2025/9/2
 * 描述： 广东水文平台测站类型枚举
 */
enum class GuangdongWaterPlatformStationType(
    private val stationName: String,
    private val code: String
) {
    FLOOD_MONITORING("山洪灾害监测站", "0"),
    RIVER_COURSE_MONITORING("河道水情监测站", "1"),
    SETTLEMENT_MONITORING("沉降监测站", "3"),
    WATER_QUALITY_MONITORING("水质监测站", "4"),
    RAIN_MONITORING("雨量监测站", "5"),
    FLOW_MONITORING("流量监测站", "6"),
    BDS_FLOOD_MONITORING("北斗山洪灾害监测站", "7")

;

    fun getStationName(): String {
        return stationName
    }

    fun getCode(): String {
        return code
    }

    companion object {
        @JvmStatic
        fun valueByStationName(name: String): GuangdongWaterPlatformStationType {
            if (TextUtils.isEmpty(name)) return FLOOD_MONITORING
            for (station in entries) {
                if (station.stationName == name) return station
            }
            return FLOOD_MONITORING
        }

        @JvmStatic
        fun valueByCode(code: String): GuangdongWaterPlatformStationType {
            if (TextUtils.isEmpty(code)) return FLOOD_MONITORING
            for (station in entries) {
                if (station.code == code) return station
            }
            return FLOOD_MONITORING
        }

        @JvmStatic
        val stationNames: List<String>
            get() = entries.map { it.stationName }
    }
}