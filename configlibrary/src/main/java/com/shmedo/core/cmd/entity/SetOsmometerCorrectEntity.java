package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2018/1/8.
 * 设置数字渗压计深度、温度修正值参数
 */
public class SetOsmometerCorrectEntity implements Validater {
    private double depthCorrect; //深度修正值 mm
    private double temperatureCorrect; //温度修正值 ℃

    public SetOsmometerCorrectEntity(double depthCorrect, double temperatureCorrect) {
        this.depthCorrect = depthCorrect;
        this.temperatureCorrect = temperatureCorrect;
    }

    @Override
    public void validate() {
        if ( depthCorrect > 65535)
            throw new DASParameterException("深度触发参数错误");

        if (temperatureCorrect < 0 || temperatureCorrect > 65535)
            throw new DASParameterException("温度触发参数错误");
    }

    @Override
    public String toString() {
        return depthCorrect + "," + temperatureCorrect;
    }
}
