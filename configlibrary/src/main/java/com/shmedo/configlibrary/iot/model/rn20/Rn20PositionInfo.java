package com.shmedo.configlibrary.iot.model.rn20;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/4 <br/>
 * 描述：     TODO
 */
public class Rn20PositionInfo {
    private String longitude;
    private String latitude;

    public String getLongitude() {
        return TextUtils.isEmpty(longitude) ? "0" : longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return TextUtils.isEmpty(latitude) ? "0" : latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
