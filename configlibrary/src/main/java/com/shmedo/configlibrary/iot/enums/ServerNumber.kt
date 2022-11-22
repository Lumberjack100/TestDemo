package com.shmedo.configlibrary.iot.enums;

import java.io.Serializable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/16/20 <br/>
 * 描述：     服务器(数据中心)编号
 */
public enum ServerNumber implements Serializable {
    NUMBER_ONE(1),
    NUMBER_TWO(2),
    NUMBER_THREE(3),
    NUMBER_FOUR(4);

    private int number;

    ServerNumber(int number) {
        this.number = number;
    }

    public int toInt() {
        return number;
    }

    public static ServerNumber value(int port) {
        switch (port) {
            case 1:
                return NUMBER_ONE;
            case 2:
                return NUMBER_TWO;
            case 3:
                return NUMBER_THREE;
            case 4:
                return NUMBER_FOUR;
            default:
                return NUMBER_ONE;
        }
    }
}
