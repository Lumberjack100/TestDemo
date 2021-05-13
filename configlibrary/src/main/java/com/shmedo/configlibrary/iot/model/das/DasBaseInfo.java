package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/15 <br/>
 * 描述：    DAS 状态页面基本信息
 */
public class DasBaseInfo {
    private String sn;//SN号
    private String iccid;//SIM卡识别码
    private String imei;//IMEI号
    private String ver;//固件版本
    private String local;//位置
    private String involt;//内部电量
    private String outvolt;//外部电压
    private String csq;//信号强度
    private String isp;//网络运营商
    private String code;//设备启动代码

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

    public String getVer() {
        return TextUtils.isEmpty(ver) ? "" : ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getLocal() {
        return TextUtils.isEmpty(local) ? "" : local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getInvolt() {
        return TextUtils.isEmpty(involt) ? "0" : involt;
    }

    public void setInvolt(String involt) {
        this.involt = involt;
    }

    public String getOutvolt() {
        return TextUtils.isEmpty(outvolt) ? "0" : outvolt;
    }

    public void setOutvolt(String outvolt) {
        this.outvolt = outvolt;
    }

    public String getCsq() {
        return TextUtils.isEmpty(csq) ? "" : csq;
    }

    public void setCsq(String csq) {
        this.csq = csq;
    }

    public String getIsp() {
        return TextUtils.isEmpty(isp) ? "" : isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getCode() {
        return TextUtils.isEmpty(code) ? "" : code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
