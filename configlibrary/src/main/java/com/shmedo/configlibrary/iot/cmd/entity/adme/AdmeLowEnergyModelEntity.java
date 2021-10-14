package com.shmedo.configlibrary.iot.cmd.entity.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/24/20 <br/>
 * 描述：     ADME 低功耗模式
 */
public class AdmeLowEnergyModelEntity implements Validater {
    private String model;


    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(model))
            throw new DASParameterException("ADME 模式不正确");
    }

    @Override
    public String toString() {
        return "model=" + model;
    }
}
