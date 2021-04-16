package com.shmedo.configlibrary.iot.enums;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/27/20 <br/>
 * 描述：     Vms 终端接入的传感器计算方式
 */
public enum VmsSensorCalculation {
    /**
     * 直线式(振弦式传感器)
     */
    LINEAR("58"),

    /**
     * 多项式(振弦式传感器)
     */
    POLYNOMIAL("55"),

    /**
     * 数字式
     */
    MEMS("80"),

    /**
     * 模数
     */
    MODULUS("81");

    private String type;

    VmsSensorCalculation(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }

    public static VmsSensorCalculation value(String type) {
        switch (type) {
            case "58":
                return LINEAR;

            case "55":
                return POLYNOMIAL;

            case "80":
                return MEMS;

            case "81":
                return MODULUS;

            default:
                return null;
        }
    }
}
