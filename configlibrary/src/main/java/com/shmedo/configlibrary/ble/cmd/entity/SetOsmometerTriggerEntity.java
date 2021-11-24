package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2018/1/8.
 * 设置数字水位计触发值
 */
public class SetOsmometerTriggerEntity implements Validater {
    private String depthTrigger; //深度触发值 mm
    private String temperatureTrigger; //温度触发值 ℃

    public SetOsmometerTriggerEntity(String depthTrigger, String temperatureTrigger) {
        this.depthTrigger = depthTrigger;
        this.temperatureTrigger = temperatureTrigger;
    }

    @Override
    public void validate() {
//        if (depthTrigger > 65535)
//            throw new DASParameterException("深度触发参数错误");
//
//        if (temperatureTrigger < 0 || temperatureTrigger > 65535)
//            throw new DASParameterException("温度触发参数错误");
    }

    @Override
    public String toString() {
        return depthTrigger + "," + temperatureTrigger;
    }
}
