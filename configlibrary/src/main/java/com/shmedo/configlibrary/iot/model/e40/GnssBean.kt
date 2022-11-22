package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     TODO
 */
public class GnssBean {
    private String time;
    private String lon;
    private String lat;

    public String getTime() {
        return TextUtils.isEmpty(time) ? "" : time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLon() {
        return TextUtils.isEmpty(lon) ? "" : lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return TextUtils.isEmpty(lat) ? "" : lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

}
