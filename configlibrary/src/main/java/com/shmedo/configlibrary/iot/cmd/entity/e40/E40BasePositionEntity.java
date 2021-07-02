package com.shmedo.configlibrary.iot.cmd.entity.e40;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/2 <br/>
 * 描述：      生成 E40基站位置参数拼接指令
 */
public class E40BasePositionEntity implements Validater {
    private String mode;//1表示自动模式，2表示第一次自动获取以后采用第一次值，3表示手动模式，当为自动模式时，可不设置其他参数。
    private String lon;//经度
    private String lat;//纬度
    private String alt;//高程

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mode=" + mode);
        stringBuilder.append("&");
        stringBuilder.append("lon=" + lon);
        stringBuilder.append("&");
        stringBuilder.append("lat=" + lat);
        stringBuilder.append("&");
        stringBuilder.append("alt=" + alt);

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
