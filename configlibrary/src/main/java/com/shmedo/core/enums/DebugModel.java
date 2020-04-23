package com.shmedo.core.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adu on 2017/12/11.
 * 调试模式
 */
public enum DebugModel {
    INITIALZE(0), //初始化
    CLOSE(1), //关闭
    DEBUG(2), //debug
    INFO(3);//info


    private int model;

    DebugModel(int i) {
        this.model = i;
    }

    public int toInt() {
        return model;
    }

    public static DebugModel valueOf(int model) {
        switch (model) {
            case 0:
                return INITIALZE;

            case 1:
                return CLOSE;

            case 2:
                return DEBUG;

            case 3:
                return INFO;

            default:
                return INITIALZE;
        }
    }

    public static boolean isValidMode(int value) {
        List<Integer> allModes = new ArrayList<>();
        for (DebugModel model : DebugModel.values()) {
            allModes.add(model.toInt());
        }

        return allModes.contains(value);
    }
}
