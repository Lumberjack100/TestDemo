package com.shmedo.configlibrary.iot.enums;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/11/23 <br/>
 * 描述：   ADME 模块异常信息
 */
public enum AdmeModuleErrorType {

    VOLT_POWER_UNDER("1", "设备欠压"),

    VOLT_POWER_OVER("2", "设备过压"),

    VOLT_SENSOR_UNDER("3", "测斜仪欠压"),

    FAIL("4", "测斜仪配对失败"),

    OVER_C_SF("5", "伺服电机过流"),

    OVER_T_SF("6", "伺服电机过力矩"),

    DZ_SF("7", "伺服电机低力矩"),

    OVER_V_SF("8", "伺服电机超速"),

    ERROR_WIRING_SF("9", "伺服电机接线错误"),

    DZ_JMQ("10", "计米器堵转"),

    FZ_RUN_JMQ("11", "计米器反转"),

    ERROR_WIRING_JMQ("12", "计米器接线错误"),

    INSUFFICIENT_BASE_PULSE_JMQ("13", "计米器基数脉冲不足");


    AdmeModuleErrorType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AdmeModuleErrorType valueByCode(String code) {
        if (TextUtils.isEmpty(code))
            return null;

        for (AdmeModuleErrorType errorType : AdmeModuleErrorType.values()) {
            if (errorType.getCode().equals(code))
                return errorType;
        }

        return null;
    }

    public static AdmeModuleErrorType valueByDesc(String desc) {
        if (TextUtils.isEmpty(desc))
            return null;

        for (AdmeModuleErrorType sensorType : AdmeModuleErrorType.values()) {
            if (sensorType.getDescription().equals(desc))
                return sensorType;
        }

        return null;
    }
}
