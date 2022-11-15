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
     * SL651 水文协议特有配置参数
     */
    private String type_code;//测站编码
    private String co_address;//中心站地址
    private String password;//密码
    private String taddress;//遥测站地址
    private String hour_report;//小时报开启标识  1:开启 0:关闭
    private String data_link;//数据链路维持报  0|[10,40]   0:关闭



    public void setServerNumber(ServerNumber serverNumber) {
        this.serverNumber = serverNumber;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public void setPlattype(String plattype) {
        this.plattype = plattype;
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

    public void setType_code(String type_code) {
        this.type_code = type_code;
    }

    public void setCo_address(String co_address) {
        this.co_address = co_address;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTaddress(String taddress) {
        this.taddress = taddress;
    }

    public void setHour_report(String hour_report) {
        this.hour_report = hour_report;
    }

    public void setData_link(String data_link) {
        this.data_link = data_link;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("centerid=" + serverNumber.toInt());
        stringBuilder.append("&");
        if (protocol != null) {
            stringBuilder.append("protocol=" + (TextUtils.isEmpty(protocol) ? "" : protocol));
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(datatype)) {
            stringBuilder.append("datatype=" + datatype);
            stringBuilder.append("&");
        }
        if (plattype != null) {
            stringBuilder.append("plattype=" + plattype);
            stringBuilder.append("&");
        }
        if (addr != null) {
            stringBuilder.append("addr=" + addr);
            stringBuilder.append("&");
        }
        if (port != null) {
            stringBuilder.append("port=" + port);
            stringBuilder.append("&");
        }
        if (deviceid != null) {
            stringBuilder.append("deviceid=" + deviceid);
            stringBuilder.append("&");
        }
        if (devicekey != null) {
            stringBuilder.append("devicekey=" + devicekey);
            stringBuilder.append("&");
        }
        if (httpaddr != null) {
            stringBuilder.append("httpaddr=" + httpaddr);
            stringBuilder.append("&");
        }
        if (httpport != null) {
            stringBuilder.append("httpport=" + httpport);
            stringBuilder.append("&");
        }
        if (projid != null) {
            stringBuilder.append("projid=" + projid);
            stringBuilder.append("&");
        }
        if (regcode != null) {
            stringBuilder.append("regcode=" + regcode);
            stringBuilder.append("&");
        }

        if (type_code != null) {
            stringBuilder.append("type_code=" + type_code);
            stringBuilder.append("&");
        }
        if (co_address != null) {
            stringBuilder.append("co_address=" + co_address);
            stringBuilder.append("&");
        }
        if (password != null) {
            stringBuilder.append("password=" + password);
            stringBuilder.append("&");
        }
        if (taddress != null) {
            stringBuilder.append("taddress=" + taddress);
            stringBuilder.append("&");
        }
        if (hour_report != null) {
            stringBuilder.append("hour_report=" + hour_report);
            stringBuilder.append("&");
        }
        if (data_link != null) {
            stringBuilder.append("data_link=" + data_link);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
