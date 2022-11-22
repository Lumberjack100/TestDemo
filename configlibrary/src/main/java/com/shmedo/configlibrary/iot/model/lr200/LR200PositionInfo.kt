package com.shmedo.configlibrary.iot.model.lr200;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/25 <br/>
 * 描述：     TODO
 */
public class LR200PositionInfo {
    private String lng;
    private String lat;


    public String getLng() {
        return TextUtils.isEmpty(lng) ? "0" : lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return TextUtils.isEmpty(lat) ? "0" : lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }
}
