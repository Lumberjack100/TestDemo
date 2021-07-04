package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/4 <br/>
 * 描述：     NMEA输出内容及输出频率
 */
public class E40NmeaTimeInfo {
    private String gga;
    private String rmc;
    private String vtg;
    private String gsv;
    private String gsa;

    public String getGga() {
        return TextUtils.isEmpty(gga) ? "0" : gga;
    }

    public void setGga(String gga) {
        this.gga = gga;
    }

    public String getRmc() {
        return TextUtils.isEmpty(rmc) ? "0" : rmc;
    }

    public void setRmc(String rmc) {
        this.rmc = rmc;
    }

    public String getVtg() {
        return TextUtils.isEmpty(vtg) ? "0" : vtg;
    }

    public void setVtg(String vtg) {
        this.vtg = vtg;
    }

    public String getGsv() {
        return TextUtils.isEmpty(gsv) ? "0" : gsv;
    }

    public void setGsv(String gsv) {
        this.gsv = gsv;
    }

    public String getGsa() {
        return TextUtils.isEmpty(gsa) ? "0" : gsa;
    }

    public void setGsa(String gsa) {
        this.gsa = gsa;
    }
}
