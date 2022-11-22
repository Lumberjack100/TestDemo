package com.shmedo.configlibrary.iot.model.e40;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/24 <br/>
 * 描述：     TODO
 */
public class SatelitteBean {
    @SerializedName("GPS")
    private List<GPSBean> gpsBeanList;

    @SerializedName("GLO")
    private List<GLOBean> gloBeanList;

    @SerializedName("BDS")
    private List<BDSBean> bdsBeanList;

    public List<GPSBean> getGpsBeanList() {
        return gpsBeanList == null ? new ArrayList<GPSBean>() : gpsBeanList;
    }

    public void setGpsBeanList(List<GPSBean> gpsBeanList) {
        this.gpsBeanList = gpsBeanList;
    }

    public List<GLOBean> getGloBeanList() {
        return gloBeanList == null ? new ArrayList<GLOBean>() : gloBeanList;
    }

    public void setGloBeanList(List<GLOBean> gloBeanList) {
        this.gloBeanList = gloBeanList;
    }

    public List<BDSBean> getBdsBeanList() {
        return bdsBeanList == null ? new ArrayList<BDSBean>() : bdsBeanList;
    }

    public void setBdsBeanList(List<BDSBean> bdsBeanList) {
        this.bdsBeanList = bdsBeanList;
    }
}
