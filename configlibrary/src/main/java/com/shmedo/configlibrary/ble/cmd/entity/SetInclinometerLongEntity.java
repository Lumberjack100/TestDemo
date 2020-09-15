package com.shmedo.configlibrary.ble.cmd.entity;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by adu on 2017/12/28.
 * 设置测斜仪测段长参数（单位MM）（测斜采集器特有参数）
 */
public class SetInclinometerLongEntity implements Validater {
    private String sensorType;  //传感器类型
    private String measSegment;     //测长

    public SetInclinometerLongEntity(String sensorType, String measSegment) {
        this.sensorType = sensorType;
        this.measSegment = measSegment;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(sensorType) && TextUtils.isEmpty(measSegment))
            throw new DASParameterException("参数不能为空");

        if (!SensorType.isValidSensor(sensorType))
            throw new DASParameterException("传感器不存在");
    }

    @Override
    public String toString() {
        return this.sensorType+""+this.measSegment;
    }
}
