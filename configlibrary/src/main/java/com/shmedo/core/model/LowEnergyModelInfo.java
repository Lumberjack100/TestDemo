package com.shmedo.core.model;


import com.shmedo.core.enums.LowEnergyModel;

/**
 * Created by adu on 2017/12/18.
 * DAS低功耗模式实体类
 */
public class LowEnergyModelInfo {
    private LowEnergyModel lowEnergyModel;

    public LowEnergyModel getLowEnergyModel() {
        return lowEnergyModel;
    }

    public void setLowEnergyModel(LowEnergyModel lowEnergyModel) {
        this.lowEnergyModel = lowEnergyModel;
    }

    @Override
    public String toString() {
        return "DASWorkModelInfo{" +
                "dasWorkModel=" + lowEnergyModel +
                '}';
    }
}
