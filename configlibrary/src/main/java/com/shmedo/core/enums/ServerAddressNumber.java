package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/15.
 * 服务器地址中服务器编号
 */
public enum ServerAddressNumber {
    NUMBER_ONE(1),
    NUMBER_TWO(2),
    NUMBER_THREE(3);

    private int number;

    ServerAddressNumber(int number) {
        this.number = number;
    }

    public int toInt() {
        return number;
    }

    public static ServerAddressNumber valueOf(int port) {
        switch (port) {
            case 1:
                return NUMBER_ONE;
            case 2:
                return NUMBER_TWO;
            case 3:
                return NUMBER_THREE;
            default:
                return NUMBER_ONE;
        }
    }

}
