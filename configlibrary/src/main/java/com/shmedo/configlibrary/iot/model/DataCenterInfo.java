package com.shmedo.configlibrary.iot.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/18/20 <br/>
 * 描述：     数据中心参数信息
 */
public class DataCenterInfo {
    private String centerid;
    private String protocol;//传输协议;TCP-C/TCP-S/MQTT
    private String datatype;//数据协议,由设备类型决定
    private String plattype;//平台类型
    private String addr;//数据中心地址,addr和port设置为空时，关闭该数据中心
    private String port;//数据中心端口

    /**
     * MQTT 协议特有配置参数
     */
    private String deviceid;//设备id（MQTT参数）,设备id、key设置为空时，设备通过自动注册的方式获取id、key
    private String devicekey;//设备key（MQTT参数）
    private String httpaddr;//设备注册HTTP地址，域名或者IP（MQTT参数）,MQTT协议下，设备通过自动注册的方式获取到设备id，key
    private String httpport;//设备注册HTTP端口（MQTT参数）
    private String projid;//产品ID(MQTT参数)
    private String regcode;//厂商设备注册码（MQTT参数）

    /**
     * SL651水文协议特有配置参数
     */
    private String type_code;//测站编码
    private String co_address;//中心站地址
    private String password;//密码
    private String taddress;//遥测站地址
    private String hour_report;//小时报开启标识  1:开启 0:关闭
    private String data_link;//数据链路维持报  0|[10,40]   0:关闭


    public String getCenterid() {
        return TextUtils.isEmpty(centerid) ? "" : centerid;
    }

    public void setCenterid(String centerid) {
        this.centerid = centerid;
    }

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

    public String getPlattype() {
        return TextUtils.isEmpty(plattype) ? "" : plattype;
    }

    public void setPlattype(String plattype) {
        this.plattype = plattype;
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

    public String getType_code() {
        return type_code;
    }

    public void setType_code(String type_code) {
        this.type_code = type_code;
    }

    public String getCo_address() {
        return co_address;
    }

    public void setCo_address(String co_address) {
        this.co_address = co_address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTaddress() {
        return taddress;
    }

    public void setTaddress(String taddress) {
        this.taddress = taddress;
    }

    public String getHour_report() {
        return TextUtils.isEmpty(hour_report) ? "" : hour_report;
    }

    public void setHour_report(String hour_report) {
        this.hour_report = hour_report;
    }

    public String getData_link() {
        return TextUtils.isEmpty(data_link) ? "" : data_link;
    }

    public void setData_link(String data_link) {
        this.data_link = data_link;
    }
}
