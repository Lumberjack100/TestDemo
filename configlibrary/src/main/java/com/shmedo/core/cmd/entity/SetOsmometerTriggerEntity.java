package com.shmedo.core.cmd.entity;

import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

/**
 * Created by adu on 2018/1/8.
 * 设置数字渗压计触发值
 */
public class SetOsmometerTriggerEntity implements Validater {
    private double depthTrigger; //深度触发值 mm
    private double temperatureTrigger; //温度触发值 ℃

    public SetOsmometerTriggerEntity(double depthTrigger, double temperatureTrigger) {
        this.depthTrigger = depthTrigger;
        this.temperatureTrigger = temperatureTrigger;
    }

    @Override
    public void validate() {
        if (depthTrigger > 65535)
            throw new DASParameterException("深度触发参数错误");

        if (temperatureTrigger < 0 || temperatureTrigger > 65535)
            throw new DASParameterException("温度触发参数错误");
    }

    @Override
    public String toString() {
        return depthTrigger + "," + temperatureTrigger;
    }
}
