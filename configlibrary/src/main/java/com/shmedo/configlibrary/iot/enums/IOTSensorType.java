package com.shmedo.configlibrary.iot.enums;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adu on 2017/12/14.
 * 传感器类型
 */
public enum IOTSensorType {
    /**
     * 压电式雨量计
     */
    RAIN_GAUGE("1"),

    /**
     * 拉线位移计 MPS-M-2000
     */
    WIRE_SHIFT("2"),

    /**
     * 土壤含水率 TR-3000
     */
    SOIL_MOISTURE("3"),

    /**
     * 测斜仪 I-P-I
     */
    INCLINOMETER("4"),

    /**
     * 超声波物位计 HBRD908
     */
    ULTRASONIC_LEVEL_GAUGE("6"),

    /**
     * 雷达物位计 MH-A15R
     */
    RADAR_LEVEL_GAUGE("7"),

    /**
     * 墒情计 EP100G
     */
    MOISTURE_METER("8"),

    /**
     * 温湿度计 CSW18
     */
    TEMPERATURE_HUMIDITY_METER("12"),

    /**
     * 扬压力计 VWP-G
     */
    UPLIFT_PRESSURE_GAUGE("15"),

    /**
     * 陆岩倾角仪 LY215
     */
    LUYAN_INCLINOMETER("16"),

    /**
     * 次声传感器
     */
    INFRASOUND_SENSOR("21"),

    /**
     * 量水堰传感器
     */
    WEIR_SENSOR("22"),

    /**
     * 气象站
     */
    WEATHER_STATION("25"),

    /**
     * 浊度仪传感器
     */
    TURBIDITY_METER_SENSOR("26"),

    /**
     * 基康渗压计 BGK-4500
     */
    KANG_PERCOLATE("50"),

    /**
     * 葛南渗压计 VWP-03
     */
    GUDAN_PERCOLATE("51"),

    /**
     * 葛南土压力盒 VWE-0.6
     */
    GUDAN_SOIL_PRESSURE("52"),

    /**
     * 葛南应力计 VWS-15
     */
    GUDAN_STRESS("53"),

    /**
     * 葛南无应力计 VWS-15M
     */
    GUDAN_NOT_STRESS("54"),

    /**
     * 葛南位移计 VWD-100
     */
    GUDAN_DISPLACEMENT_METER("55"),

    /**
     * 轴力计 ZLJ-300T
     */
    JUNXING_ZLJ_300T("58"),

    UNKNOWN_TYPE("-1");

    private String type;

    IOTSensorType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }

    public static IOTSensorType value(String type) {
        if (TextUtils.isEmpty(type))
            return UNKNOWN_TYPE;

        for (IOTSensorType sensorType : IOTSensorType.values()) {
            if(sensorType.type.equals(type))
                return sensorType;
        }

        return UNKNOWN_TYPE;
    }

    public static boolean isValidSensor(String coll) {
        if (TextUtils.isEmpty(coll))
            return false;

        List<String> allSensors = new ArrayList<>();
        for (IOTSensorType type : IOTSensorType.values()) {
            allSensors.add(type.toString());
        }
        return allSensors.contains(coll);
    }
}
