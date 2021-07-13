package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/24/20 <br/>
 * 描述：    生成ADME 基础配置参数拼接指令
 */
public class AdmeBasicConfigEntity implements Validater {
    private String inctype;//测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    private String address;//采集器 / MAC 地址
    private String interdeep;//测斜管孔深
    private String downspeed;//下放速度
    private String downwaitetime;//下放等待时间
    private String datatype;//数据结算方式（0:顶固定法，1底固定法）

    public void setInctype(String inctype) {
        this.inctype = inctype;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setInterdeep(String interdeep) {
        this.interdeep = interdeep;
    }

    public void setDownspeed(String downspeed) {
        this.downspeed = downspeed;
    }

    public void setDownwaitetime(String downwaitetime) {
        this.downwaitetime = downwaitetime;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        if (inctype != null && !inctype.equals("NullKey")) {
            stringBuilder.append("inctype=" + inctype);
            stringBuilder.append("&");
        }
        if (address != null && !address.equals("NullKey")) {
            stringBuilder.append("address=" + address);
            stringBuilder.append("&");
        }
        if (interdeep != null && !interdeep.equals("NullKey")) {
            stringBuilder.append("interdeep=" + interdeep);
            stringBuilder.append("&");
        }
        if (downspeed != null && !downspeed.equals("NullKey")) {
            stringBuilder.append("downspeed=" + downspeed);
            stringBuilder.append("&");
        }
        if (downwaitetime != null && !downwaitetime.equals("NullKey")) {
            stringBuilder.append("downwaitetime=" + downwaitetime);
            stringBuilder.append("&");
        }
        if (datatype != null && !datatype.equals("NullKey")) {
            stringBuilder.append("datatype=" + datatype);
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
