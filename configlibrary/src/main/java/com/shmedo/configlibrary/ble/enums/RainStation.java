package com.shmedo.configlibrary.ble.enums;

/**
 * Created by adu on 2017/12/15.
 * 雨量站
 */
public enum RainStation {

    /**
     * 开启
     */
    OPEN(1),

    /**
     * 关闭
     */
    CLOSE(2),

    /**
     * 断线报警器打开
     */
    ALARM_OPEN(3);

    private int state;
    RainStation(int state) {
        this.state = state;
    }

    public int toInt() {
        return state;
    }

    public static RainStation value(int state) {
        switch (state) {
            case 1:
                return OPEN;
            case 2:
                return CLOSE;
            case 3:
                return ALARM_OPEN;
            default:
                return OPEN;
        }
    }
}
