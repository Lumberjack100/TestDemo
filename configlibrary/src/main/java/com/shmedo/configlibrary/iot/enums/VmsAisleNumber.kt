package com.shmedo.configlibrary.iot.enums;

import java.io.Serializable;

/**
 * Vms网关通道编号
 */
public enum VmsAisleNumber implements Serializable {
    NUMBER_ONE(0),
    NUMBER_TWO(1),
    NUMBER_THREE(2);

    private int number;

    VmsAisleNumber(int number) {
        this.number = number;
    }

    public int toInt() {
        return number;
    }

    public static VmsAisleNumber value(int port) {
        switch (port) {
            case 0:
                return NUMBER_ONE;
            case 1:
                return NUMBER_TWO;
            case 2:
                return NUMBER_THREE;
            default:
                return NUMBER_ONE;
        }
    }

}
