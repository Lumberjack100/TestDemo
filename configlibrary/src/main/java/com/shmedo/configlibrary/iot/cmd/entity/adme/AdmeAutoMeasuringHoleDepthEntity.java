package com.shmedo.configlibrary.iot.cmd.entity.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/10/22 <br/>
 * 描述：     TODO
 */
public class AdmeAutoMeasuringHoleDepthEntity implements Validater {
    private String motorspeed;//电机下放速度
    private String safedistance;//安全距离补偿

    public void setMotorspeed(String motorspeed) {
        this.motorspeed = motorspeed;
    }

    public void setSafedistance(String safedistance) {
        this.safedistance = safedistance;
    }


    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("motorspeed=" + motorspeed);
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(safedistance)) {
            stringBuilder.append("safedistance=" + safedistance);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
