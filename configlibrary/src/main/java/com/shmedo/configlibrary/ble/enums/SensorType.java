package com.shmedo.configlibrary.ble.enums;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adu on 2017/12/14.
 * 传感器类型
 */
public enum SensorType {
    /**
     * 压电式雨量计
     */
    RAIN_GAUGE("01"),
    /**
     * 拉线位移计 MPS-M-2000
     */
    WIRE_SHIFT("02"),
    /**
     * 土壤含水率 TR-3000
     */
    SOIL_MOISTURE("03"),
    /**
     * 测斜仪 I-P-I
     */
    INCLINOMETER("04"),
    /**
     * 超声波物位计 HBRD908
     */
    ULTRASONIC_LEVEL_GAUGE("06"),
    /**
     * 雷达物位计 MH-A15R
     */
    RADAR_LEVEL_GAUGE("07"),
    /**
     * 墒情计 EP100G
     */
    MOISTURE_METER("08"),
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
     * 气象站
     */
    WEATHER_STATION("25"),
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
    JUNXING_ZLJ_300T("58");

    private String type;

    SensorType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }

    public static SensorType value(String type) {
        switch (type) {
            case "01":
                return RAIN_GAUGE;
            case "02":
                return WIRE_SHIFT;
            case "03":
                return SOIL_MOISTURE;
            case "04":
                return INCLINOMETER;
            case "06":
                return ULTRASONIC_LEVEL_GAUGE;
            case "07":
                return RADAR_LEVEL_GAUGE;
            case "08":
                return MOISTURE_METER;
            case "12":
                return TEMPERATURE_HUMIDITY_METER;
            case "15":
                return UPLIFT_PRESSURE_GAUGE;
            case "16":
                return LUYAN_INCLINOMETER;
            case "21":
                return INFRASOUND_SENSOR;
            case "25":
                return WEATHER_STATION;
            case "50":
                return KANG_PERCOLATE;
            case "51":
                return GUDAN_PERCOLATE;
            case "52":
                return GUDAN_SOIL_PRESSURE;
            case "53":
                return GUDAN_STRESS;
            case "54":
                return GUDAN_NOT_STRESS;
            case "55":
                return GUDAN_DISPLACEMENT_METER;
            case "58":
                return JUNXING_ZLJ_300T;
            default:
                return null;
        }
    }

    public static boolean isValidSensor(String coll) {
        if (TextUtils.isEmpty(coll))
            return false;

        List<String> allSensors = new ArrayList<>();
        for (SensorType type : SensorType.values()) {
            allSensors.add(type.toString());
        }
        return allSensors.contains(coll);
    }
}
