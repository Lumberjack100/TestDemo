package com.shmedo.lib.cmd.base.iot_cmd.enums

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/9/26 <br></br>
 * 描述：    SL651水文 遥测站分类码
 */
enum class SL651StationType(val stationName: String, val code: String) {
    PRECIPITATION("降水", "P"),
    RIVER_COURSE("河道", "H"),
    RESERVOIR("水库(湖泊)", "K"),
    DAM(
        "闸坝",
        "Z"
    ),
    PUMPING_STATION("泵站", "D"),
    TIDAL("潮汐", "T"),
    MOISTURE("墒情", "M"),
    GROUNDWATER(
        "地下水",
        "G"
    ),
    WATER_QUALITY("水质", "Q"),
    WATER_INTAKE("取水口", "I"),
    DRAIN("排水口", "O"),
    CUSTOM("自定义", "1");

    companion object {
        @JvmStatic
        fun valueByStationName(name: String): SL651StationType {
            if (TextUtils.isEmpty(name)) return CUSTOM
            for (errorType in entries) {
                if (errorType.stationName == name) return errorType
            }
            return CUSTOM
        }

        @JvmStatic
        fun valueByCode(code: String): SL651StationType {
            if (TextUtils.isEmpty(code)) return CUSTOM
            for (errorType in entries) {
                if (errorType.code == code) return errorType
            }
            return CUSTOM
        }

        @JvmStatic
        val descriptions: List<String>
            get() = entries.map { it.stationName }
    }
}