package com.shmedo.configlibrary.iot.cmd.entity;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;
import com.shmedo.configlibrary.iot.enums.ServerNumber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/18/20 <br/>
 * 描述：     设置数据中心参数
 */
public class DataCenterEntity implements Validater {
    private ServerNumber serverNumber;
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

    public void setServerNumber(ServerNumber serverNumber) {
        this.serverNumber = serverNumber;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public void setDevicekey(String devicekey) {
        this.devicekey = devicekey;
    }

    public void setHttpaddr(String httpaddr) {
        this.httpaddr = httpaddr;
    }

    public void setHttpport(String httpport) {
        this.httpport = httpport;
    }

    public void setProjid(String projid) {
        this.projid = projid;
    }

    public void setRegcode(String regcode) {
        this.regcode = regcode;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("centerid=" + serverNumber.toInt());
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(protocol)) {
            stringBuilder.append("protocol=" + protocol);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(datatype)) {
            stringBuilder.append("datatype=" + datatype);
            stringBuilder.append("&");
        }
        stringBuilder.append("addr=" + addr);
        stringBuilder.append("&");
        stringBuilder.append("port=" + port);
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(deviceid)) {
            stringBuilder.append("deviceid=" + deviceid);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(devicekey)) {
            stringBuilder.append("devicekey=" + devicekey);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(httpaddr)) {
            stringBuilder.append("httpaddr=" + httpaddr);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(httpport)) {
            stringBuilder.append("httpport=" + httpport);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(projid)) {
            stringBuilder.append("projid=" + projid);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(regcode)) {
            stringBuilder.append("regcode=" + regcode);
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
