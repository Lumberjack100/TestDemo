package com.shmedo.core.cmd.entity;

import android.text.TextUtils;

import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adu on 2017/12/19.
 *  设置采集器接入的传感器参数
 */
public class SetCollectorSensorEntity implements Validater{
    private String number;
    private String sensorNumber;
    private String addressType;

    public SetCollectorSensorEntity(String number, String sensorNumber, String addressType) {
        this.number = number;
        this.sensorNumber = sensorNumber;
        this.addressType = addressType;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(number) && TextUtils.isEmpty(sensorNumber) && TextUtils.isEmpty(addressType))
            throw new DASParameterException("参数错误");
        if(!CollectorModel.isValidCollector(this.number))
            throw new DASParameterException("采集器不存在");
        if (!isValidNumber(sensorNumber))
            throw new DASParameterException("参数错误");
    }

    @Override
    public String toString() {
        return number+""+sensorNumber+""+addressType;
    }

    /**
     * 判断传感器的取值范围
     * @param sensorNumber
     * @return 返回参数范围是否正确
     */
    private Boolean isValidNumber(String sensorNumber){
        Pattern p = Pattern.compile("^0[0-8]$");
        Matcher m = p.matcher(sensorNumber);
        return m.matches();
    }
}
