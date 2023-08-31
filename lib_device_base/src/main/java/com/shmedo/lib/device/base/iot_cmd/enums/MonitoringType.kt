package com.shmedo.lib.device.base.iot_cmd.enums

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/11/4 <br></br>
 * 描述：    监测类型
 */
enum class MonitoringType(val code: String, val description: String) {
    DEVICE_STATUS("000", "设备状态"),
    SURFACE_CRACK_METER("101", "地表裂缝计"),
    WALL_CRACK_METER("102", "墙裂缝计"),
    ACCELEROMETER("103", "加速度计"),
    MUD_LEVEL_METER("104", "泥位计"),
    WARNING_HORN("105", "预警喇叭"),
    RAIN_METER("201", "雨量计"),
    SOIL_MOISTURE_METER("202", "土壤含水率"),
    CRACK_METER("203", "裂缝计"),
    GNSS_RESULT_DATA("204", "GNSS结果数据"),
    GNSS_RAW_DATA("205", "GNSS原始数据"),
    INCLINOMETER_METER("206", "倾角计"),
    WATER_PRESSURE_METER("207", "水压力计"),
    INFRASOUND_METER("208", "次声仪"),
    EARTH_PRESSURE_METER("209", "土压力计"),
    VIBRATING_WIRE_STRAIN_METER("210", "振弦式应力计"),
    GROUND_DISPLACEMENT_METER("211", "地表位移计"),
    DEEP_DISPLACEMENT_METER("212", "深部位移计"),
    QING_XIE_METER("213", "倾斜仪"),
    WATER_LEVEL_METER("214", "水位计"),
    MULTI_POINT_DISPLACEMENT_METER("215", "多点位移计"),
    OSMOMETER("216", "渗压计"),
    FLOW_METER("217", "流速仪"),
    TEMPERATURE_METER("218", "温度计"),
    TDR_DEFORMATION_METER("219", "TDR变形计"),
    SPRING_WATER_FLOW_METER("220", "泉水流量计"),
    SEDIMENTATION_INSTRUMENT("221", "沉降仪"),
    BOREHOLE_INCLINOMETER("222", "钻孔测斜仪"),
    PUMPING_STATION_DATA("10000", "泵站数据"),
    PICTURE("10001", "图片"),
    DRY_BEACH("10002", "干滩计"),
    TURBIDITY_METER("10003", "浊度计"),
    HUMIDITY_METER("10004", "湿度计"),
    AXIAL_FORCE_METER("10005", "钢筋计"),
    AXIAL_FORCE_METER_MODULUS("10066", "表面应变计"),
    PH_METER("10006", "PH计"),
    AVALANCHE_METER("10008", "崩滑仪"),
    DEVICE_LOG("20001", "设备日志"),
    DEVICE_ALARM("20002", "设备报警"),
    UNKNOWN_TYPE("-1", "未知类型");

    override fun toString(): String {
        return code
    }

    companion object {
        @JvmStatic
        fun valueByCode(code: String): MonitoringType {
            var code = code
            if (TextUtils.isEmpty(code)) return UNKNOWN_TYPE
            if (code.contains("_")) {
                val strs = code.split("_").toTypedArray()
                code = strs[0]
            }
            for (sensorType in values()) {
                if (sensorType.code == code) return sensorType
            }
            return UNKNOWN_TYPE
        }

        @JvmStatic
        fun valueByDesc(desc: String): MonitoringType {
            if (TextUtils.isEmpty(desc)) return UNKNOWN_TYPE
            for (sensorType in values()) {
                if (sensorType.description == desc) return sensorType
            }
            return UNKNOWN_TYPE
        }
    }
}