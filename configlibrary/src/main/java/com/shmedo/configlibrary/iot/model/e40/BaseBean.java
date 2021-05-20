package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     TODO
 */
public class BaseBean {
    private String sn;
    private String iccid;
    private String imei;
    private String version;
    private String oem;
    private double volt;

    public String getSn() {
        return TextUtils.isEmpty(sn) ? "" : sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getIccid() {
        return TextUtils.isEmpty(iccid) ? "" : iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getImei() {
        return TextUtils.isEmpty(imei) ? "" : imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getVersion() {
        return TextUtils.isEmpty(version) ? "" : version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOem() {
        return TextUtils.isEmpty(oem) ? "" : oem;
    }

    public void setOem(String oem) {
        this.oem = oem;
    }

    public double getVolt() {
        return volt;
    }

    public void setVolt(double volt) {
        this.volt = volt;
    }
}
