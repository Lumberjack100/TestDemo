package com.shmedo.configlibrary.iot.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/18/20 <br/>
 * 描述：     数据中心参数信息
 */
public class DataCenterInfo {
    private String protocol;//传输协议;TCP-C/TCP-S/MQTT
    private String datatype;//数据协议,由设备类型决定
    private String addr;//数据中心地址,addr和port设置为空时，关闭该数据中心
    private String port;//数据中心端口
    private String deviceid;//设备id（MQTT参数）,设备id、key设置为空时，设备通过自动注册的方式获取id、key
    private String devicekey;//设备key（MQTT参数）
    private String httpaddr;//设备注册HTTP地址，域名或者IP（MQTT参数）,MQTT协议下，设备通过自动注册的方式获取到设备id，key
    private String httpport;//设备注册HTTP端口（MQTT参数）
    private String projid;//产品ID(MQTT参数)
    private String regcode;//厂商设备注册码（MQTT参数）


    public String getProtocol() {
        return TextUtils.isEmpty(protocol) ? "" : protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDatatype() {
        return TextUtils.isEmpty(datatype) ? "" : datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getAddr() {
        return TextUtils.isEmpty(addr) ? "" : addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getPort() {
        return TextUtils.isEmpty(port) ? "" : port;

    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDeviceid() {
        return TextUtils.isEmpty(deviceid) ? "" : deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getDevicekey() {
        return TextUtils.isEmpty(devicekey) ? "" : devicekey;
    }

    public void setDevicekey(String devicekey) {
        this.devicekey = devicekey;
    }

    public String getHttpaddr() {
        return TextUtils.isEmpty(httpaddr) ? "" : httpaddr;
    }

    public void setHttpaddr(String httpaddr) {
        this.httpaddr = httpaddr;
    }

    public String getHttpport() {
        return TextUtils.isEmpty(httpport) ? "" : httpport;
    }

    public void setHttpport(String httpport) {
        this.httpport = httpport;
    }

    public String getProjid() {
        return TextUtils.isEmpty(projid) ? "" : projid;
    }

    public void setProjid(String projid) {
        this.projid = projid;
    }

    public String getRegcode() {
        return TextUtils.isEmpty(regcode) ? "" : regcode;
    }

    public void setRegcode(String regcode) {
        this.regcode = regcode;
    }
}
