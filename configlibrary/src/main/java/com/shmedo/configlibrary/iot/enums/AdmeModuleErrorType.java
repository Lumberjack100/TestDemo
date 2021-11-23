package com.shmedo.configlibrary.iot.enums;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/11/23 <br/>
 * 描述：   ADME 模块异常信息
 */
public enum AdmeModuleErrorType {

    VOLT_POWER_UNDER("VOLT_POWER_UNDER", "设备欠压"),

    VOLT_POWER_OVER("VOLT_POWER_OVER", "设备过压"),

    VOLT_SENSOR_UNDER("VOLT_SENSOR_UNDER", "测斜仪欠压"),

    FAIL("FAIL", "测斜仪配对失败"),

    OVER_C_SF("OVER_C_SF", "伺服电机过流"),

    OVER_T_SF("OVER_T_SF", "伺服电机过力矩"),

    DZ_SF("DZ_SF", "伺服电机低力矩"),

    OVER_V_SF("OVER_V_SF", "伺服电机超速"),

    ERROR_WIRING_SF("ERROR_WIRING_SF", "伺服电机接线错误"),

    DZ_JMQ("DZ_JMQ", "计米器堵转"),

    FZ_RUN_JMQ("FZ_RUN_JMQ", "计米器反转"),

    ERROR_WIRING_JMQ("ERROR_WIRING_JMQ", "计米器接线错误");


    AdmeModuleErrorType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static AdmeModuleErrorType valueByName(String name) {
        if (TextUtils.isEmpty(name))
            return null;

        for (AdmeModuleErrorType errorType : AdmeModuleErrorType.values()) {
            if (errorType.getName().equals(name))
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
