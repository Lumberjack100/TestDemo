package com.shmedo.core.model;


import com.shmedo.core.enums.DASWorkModel;

/**
 * Created by adu on 2017/12/18.
 * DAS工作模式实体类
 */
public class DASWorkModelInfo {
    private DASWorkModel dasWorkModel;

    public DASWorkModel getDasWorkModel() {
        return dasWorkModel;
    }

    public void setDasWorkModel(DASWorkModel dasWorkModel) {
        this.dasWorkModel = dasWorkModel;
    }

    @Override
    public String toString() {
        return "DASWorkModelInfo{" +
                "dasWorkModel=" + dasWorkModel +
                '}';
    }
}
