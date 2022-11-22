package com.shmedo.configlibrary.iot.cmd.entity.rn20;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/4 <br/>
 * 描述：     TODO
 */
public class Rn20PositionEntity implements Validater {
    private String longitude;
    private String latitude;

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("longitude=" + longitude);
        stringBuilder.append("&");
        stringBuilder.append("latitude=" + latitude);

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
