package com.shmedo.core.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adu on 2017/12/11.
 * 工作模式
 */
public enum WorkModel {
    INITIALZE(0), //初始化模式
    WORK(1), //工作模式
    DEBUG(2), //debug模式
    INFO(3);//info模式


    private int model;

    WorkModel(int i) {
        this.model = i;
    }

    public int toInt() {
        return model;
    }

    public static WorkModel valueOf(int model) {
        switch (model) {
            case 0:
                return INITIALZE;

            case 1:
                return WORK;

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
        for (WorkModel model : WorkModel.values()) {
            allModes.add(model.toInt());
        }

        return allModes.contains(value);
    }
}
