package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     TODO
 */
public class NetBean {
    @SerializedName("4g")
    private String _$4g;
    private int csq;
    private String isp;
    private String type;
    private int socket1;
    private int socket2;
    private int socket3;
    private int socket4;

    public String get_$4g() {
        return TextUtils.isEmpty(_$4g) ? "" : _$4g;
    }

    public void set_$4g(String _$4g) {
        this._$4g = _$4g;
    }

    public int getCsq() {
        return csq;
    }

    public void setCsq(int csq) {
        this.csq = csq;
    }

    public String getIsp() {
        return TextUtils.isEmpty(isp) ? "" : isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSocket1() {
        return socket1;
    }

    public void setSocket1(int socket1) {
        this.socket1 = socket1;
    }

    public int getSocket2() {
        return socket2;
    }

    public void setSocket2(int socket2) {
        this.socket2 = socket2;
    }

    public int getSocket3() {
        return socket3;
    }

    public void setSocket3(int socket3) {
        this.socket3 = socket3;
    }

    public int getSocket4() {
        return socket4;
    }

    public void setSocket4(int socket4) {
        this.socket4 = socket4;
    }
}
