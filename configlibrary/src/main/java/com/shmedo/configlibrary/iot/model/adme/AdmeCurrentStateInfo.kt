package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/10/21 <br/>
 * 描述：     ADME 设备当前状态
 */
public class AdmeCurrentStateInfo {
    private String sn;//设备SN号
    private String productid;//产品型号
    private String simid;//物联网卡号
    private String imeid;// IMEI卡号
    private String firversion;//固件版本
    private String ctrinputv;// CTR输入电压
    private String driveinputv;//驱动器输入电压
    private String inctype;//测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    private String incnum;//测斜仪信道号
    private String incvoltage;//测斜仪电压
    private String temperature;//设备温度
    private String humidity;//设备湿度
    private String intertempe;//测斜仪管温度
    private String signalstr;//4G信号强度
    private String incloc;// 测斜仪位置信息
    private String abndiasis;//设备异常诊断
    private String downnum;//设备下降次数
    private String testway;//工作模式(0:常规测量模式，1：特定点位模式，2：静态测量模式，3：设备停用模式)）

    public String getSn() {
        return TextUtils.isEmpty(sn) ? "" : sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getProductid() {
        return TextUtils.isEmpty(productid) ? "" : productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getSimid() {
        return TextUtils.isEmpty(simid) ? "" : simid;
    }

    public void setSimid(String simid) {
        this.simid = simid;
    }

    public String getImeid() {
        return TextUtils.isEmpty(imeid) ? "" : imeid;
    }

    public void setImeid(String imeid) {
        this.imeid = imeid;
    }

    public String getFirversion() {
        return TextUtils.isEmpty(firversion) ? "" : firversion;
    }

    public void setFirversion(String firversion) {
        this.firversion = firversion;
    }

    public String getCtrinputv() {
        return TextUtils.isEmpty(ctrinputv) ? "" : ctrinputv;
    }

    public void setCtrinputv(String ctrinputv) {
        this.ctrinputv = ctrinputv;
    }

    public String getDriveinputv() {
        return TextUtils.isEmpty(driveinputv) ? "" : driveinputv;
    }

    public void setDriveinputv(String driveinputv) {
        this.driveinputv = driveinputv;
    }

    public String getInctype() {
        return TextUtils.isEmpty(inctype) ? "" : inctype;
    }

    public void setInctype(String inctype) {
        this.inctype = inctype;
    }

    public String getIncnum() {
        return TextUtils.isEmpty(incnum) ? "" : incnum;
    }

    public void setIncnum(String incnum) {
        this.incnum = incnum;
    }

    public String getIncvoltage() {
        return TextUtils.isEmpty(incvoltage) ? "" : incvoltage;
    }

    public void setIncvoltage(String incvoltage) {
        this.incvoltage = incvoltage;
    }

    public String getTemperature() {
        return TextUtils.isEmpty(temperature) ? "" : temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return TextUtils.isEmpty(humidity) ? "" : humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getIntertempe() {
        return TextUtils.isEmpty(intertempe) ? "" : intertempe;
    }

    public void setIntertempe(String intertempe) {
        this.intertempe = intertempe;
    }

    public String getSignalstr() {
        return TextUtils.isEmpty(signalstr) ? "" : signalstr;
    }

    public void setSignalstr(String signalstr) {
        this.signalstr = signalstr;
    }

    public String getIncloc() {
        return TextUtils.isEmpty(incloc) ? "" : incloc;
    }

    public void setIncloc(String incloc) {
        this.incloc = incloc;
    }

    public String getAbndiasis() {
        return TextUtils.isEmpty(abndiasis) ? "" : abndiasis;
    }

    public void setAbndiasis(String abndiasis) {
        this.abndiasis = abndiasis;
    }

    public String getDownnum() {
        return TextUtils.isEmpty(downnum) ? "" : downnum;
    }

    public void setDownnum(String downnum) {
        this.downnum = downnum;
    }

    public String getTestway() {
        return TextUtils.isEmpty(testway) ? "" : testway;
    }

    public void setTestway(String testway) {
        this.testway = testway;
    }
}
