package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/24 <br/>
 * 描述：     TODO
 */
public class SatelitteBean {
    @SerializedName("UTCTime")
    private String utctime;

    @SerializedName("GPS")
    private List<GPSBean> gpsBeanList;

    @SerializedName("GLO")
    private List<GLOBean> gloBeanList;

    @SerializedName("BDS")
    private List<BDSBean> bdsBeanList;

    public String getUtctime() {
        return TextUtils.isEmpty(utctime) ? "" : utctime;
    }

    public void setUtctime(String utctime) {
        this.utctime = utctime;
    }

    public List<GPSBean> getGpsBeanList() {
        return gpsBeanList;
    }

    public void setGpsBeanList(List<GPSBean> gpsBeanList) {
        this.gpsBeanList = gpsBeanList;
    }

    public List<GLOBean> getGloBeanList() {
        return gloBeanList;
    }

    public void setGloBeanList(List<GLOBean> gloBeanList) {
        this.gloBeanList = gloBeanList;
    }

    public List<BDSBean> getBdsBeanList() {
        return bdsBeanList;
    }

    public void setBdsBeanList(List<BDSBean> bdsBeanList) {
        this.bdsBeanList = bdsBeanList;
    }
}
