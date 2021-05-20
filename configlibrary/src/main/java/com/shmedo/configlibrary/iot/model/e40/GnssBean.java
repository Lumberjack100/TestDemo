package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     TODO
 */
public class GnssBean {
    private String time;
    private double lon;
    private double lat;
    private List<SatelitteBean> satelitte;

    public String getTime() {
        return TextUtils.isEmpty(time) ? "" : time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public List<SatelitteBean> getSatelitte() {
        return satelitte == null ? new ArrayList<SatelitteBean>() : satelitte;
    }

    public void setSatelitte(List<SatelitteBean> satelitte) {
        this.satelitte = satelitte;
    }
}
