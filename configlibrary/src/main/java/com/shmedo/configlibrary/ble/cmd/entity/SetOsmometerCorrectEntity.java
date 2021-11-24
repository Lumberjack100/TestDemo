package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2018/1/8.
 * 设置数字水位计深度、温度修正值参数
 */
public class SetOsmometerCorrectEntity implements Validater {
    private String depthCorrect; //深度修正值 mm
    private String temperatureCorrect; //温度修正值 ℃

    public SetOsmometerCorrectEntity(String depthCorrect, String temperatureCorrect) {
        this.depthCorrect = depthCorrect;
        this.temperatureCorrect = temperatureCorrect;
    }

    @Override
    public void validate() {
//        if ( depthCorrect > 65535)
//            throw new DASParameterException("深度触发参数错误");
//
//        if (temperatureCorrect < 0 || temperatureCorrect > 65535)
//            throw new DASParameterException("温度触发参数错误");
    }

    @Override
    public String toString() {
        return depthCorrect + "," + temperatureCorrect;
    }
}
