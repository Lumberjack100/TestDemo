package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/11.
 * 调试模式
 */
public enum DebugModel {
    INITIALZE(0),CLOSE(1),DEBUG(2),INFO(3);
    //初始化、关闭、debug、info

    private int model;

    DebugModel(int i) {
        this.model = i;
    }

    public int toInt() {
        return model;
    }

    public static DebugModel valueOf(int model) {
        switch (model) {
            case 0: return INITIALZE;
            case 1: return CLOSE;
            case 2: return DEBUG;
            case 3: return INFO;
            default: return INITIALZE;
        }
    }
}
