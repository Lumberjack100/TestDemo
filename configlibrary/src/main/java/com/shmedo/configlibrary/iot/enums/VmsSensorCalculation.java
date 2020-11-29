package com.shmedo.configlibrary.iot.enums;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/27/20 <br/>
 * 描述：     Vms 终端接入的传感器计算方式
 */
public enum VmsSensorCalculation {
    /**
     * 直线式
     */
    LINEAR("58"),
    /**
     * 多项式
     */
    POLYNOMIAL("55");

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

            default:
                return null;
        }
    }
}
