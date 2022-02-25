package com.shmedo.configlibrary.iot.cmd.entity.lr200;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/25 <br/>
 * 描述：     LR200 设置位置指令参数
 */
public class LR200PositionEntity implements Validater {
    private String lng;
    private String lat;

    public void setLng(String lng) {
        this.lng = lng;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        return "lng=" + lng +
                "&" +
                "lat=" + lat;
    }
}
