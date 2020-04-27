package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/15.
 * 服务器(数据中心)编号
 */
public enum ServerNumber {
    NUMBER_ONE(1),
    NUMBER_TWO(2),
    NUMBER_THREE(3);

    private int number;

    ServerNumber(int number) {
        this.number = number;
    }

    public int toInt() {
        return number;
    }

    public static ServerNumber valueOf(int port) {
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
