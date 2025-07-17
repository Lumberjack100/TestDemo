package com.shmedo.lib.cmd.base.iot_cmd.enums

import android.text.TextUtils

/**
 * Created by adu on 2017/12/14.
 * 传感器类型
 */
enum class IOTSensorType(val code: String, val description: String) {
    /**
     * 振弦传感器
     */
    VIBRATING_SENSOR("0", "振弦传感器"),

    /**
     * 压电式雨量计
     */
    RAIN_GAUGE("1", "雨量计"),

    /**
     * 拉线位移计 MPS-M-2000
     */
    WIRE_SHIFT("2", "裂缝计"),

    /**
     * 土壤含水率 TR-3000
     */
    SOIL_MOISTURE("3", "管式含水率计"),

    /**
     * 测斜仪 I-P-I
     */
    INCLINOMETER("4", "固定测斜仪"),

    /**
     * 超声波物位计 HBRD908
     */
    ULTRASONIC_LEVEL_GAUGE("6", "超声波液(物)位计"),

    /**
     * 雷达物位计 MH-A15R
     */
    RADAR_LEVEL_GAUGE("7", "雷达液(物)位计"),

    /**
     * 墒情计 EP100G
     */
    MOISTURE_METER("8", "墒情计"),

    /**
     * 温湿度计 CSW18
     */
    TEMPERATURE_HUMIDITY_METER("12", "温湿度计"),

    /**
     * 扬压力计 VWP-G
     */
    UPLIFT_PRESSURE_GAUGE("15", "扬压力计"),

    /**
     * 陆岩倾角仪 LY215
     */
    LUYAN_INCLINOMETER("16", "倾角仪"),

    /**
     * 次声传感器
     */
    INFRASOUND("21", "次声仪"),

    /**
     * 量水堰传感器
     */
    WEIR("22", "量水堰计"),

    STATIC_LEVEL("24", "静力水准"),

    /**
     * 气象仪
     */
    WEATHER_STATION("25", "气象仪"),

    /**
     * 浊度仪传感器
     */
    TURBIDITY_METER("26", "浊度仪"),

    DIGITAL_WATER_LEVEL_GAUGE("27", "数字式水位计"),

    WATER_QUALITY_METER("28", "多参数水质仪"),

    WATER_LEVEL_GAUGE("29", "水位(液位)计"),

    SEDIMENTATION_METER("30", "沉降仪"),

    VERTICAL_COORDINATE("31", "垂线坐标仪"),

    LYSIMETER("32", "蒸渗仪"),

    /**
     * 基康渗压计 BGK-4500
     */
    KANG_PERCOLATE("50", "基康渗压计"),

    /**
     * 葛南渗压计 VWP-03
     */
    GUDAN_PERCOLATE("51", "葛南渗压计"),

    /**
     * 葛南土压力盒 VWE-0.6
     */
    GUDAN_SOIL_PRESSURE("52", "葛南土压力计"),

    /**
     * 葛南应力计 VWS-15
     */
    GUDAN_STRESS("53", "葛南应力计"),

    /**
     * 葛南无应力计 VWS-15M
     */
    GUDAN_NOT_STRESS("54", "葛南无应力计"),

    /**
     * 葛南位移计 VWD-100
     */
    GUDAN_DISPLACEMENT_METER("55", "葛南位移计"),

    /**
     * 轴力计 ZLJ-300T
     */
    JUNXING_ZLJ_300T("58", "轴力计"),

    /**
     * 通用弦式仪
     */
    GENERAL_STRING_INSTRUMENT("64", "通用弦式仪"),

    UNKNOWN_TYPE("-1", "未知类型");

    override fun toString(): String {
        return code
    }

    companion object {
        @JvmStatic
        fun value(code: String): IOTSensorType {
            if (TextUtils.isEmpty(code)) return UNKNOWN_TYPE
            for (sensorType in entries) {
                if (sensorType.code == code) return sensorType
            }
            return UNKNOWN_TYPE
        }

        @JvmStatic
        fun getSensorTypeByCollectorCode(code: String) = value(code)

        @JvmStatic
        fun getSensorTypeByDescription(desc: String): IOTSensorType {
            for (sensorType in entries) {
                if (sensorType.description == desc) return sensorType
            }
            return UNKNOWN_TYPE
        }

        //判断是否是正弦传感器
        @JvmStatic
        fun isVibratingSensor(sensorType: IOTSensorType): Boolean {
            return sensorType == VIBRATING_SENSOR || sensorType == KANG_PERCOLATE
                    || sensorType == GUDAN_PERCOLATE || sensorType == GUDAN_SOIL_PRESSURE
                    || sensorType == GUDAN_STRESS || sensorType == GUDAN_NOT_STRESS
                    || sensorType == GUDAN_DISPLACEMENT_METER || sensorType == JUNXING_ZLJ_300T
        }
    }
}