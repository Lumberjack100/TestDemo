package com.shmedo.configlibrary.iot.cmd.entity.das;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/19 <br/>
 * 描述：     生成DAS 数字水位计参数拼接指令
 */
public class DasDigitalPiezometerEntity implements Validater {
    private String sw;//0：关闭数字水位计采集功能 1：打开数字水位计采集功能
    private String addr;//地址
    private String threshold;//触发阈值
    private String corrval;//修正值
    private String ropelen;//绳长（渗压计到管口的距离）
    private String tubealti;//安装高程

    public void setSw(String sw) {
        this.sw = sw;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public void setCorrval(String corrval) {
        this.corrval = corrval;
    }

    public void setRopelen(String ropelen) {
        this.ropelen = ropelen;
    }

    public void setTubealti(String tubealti) {
        this.tubealti = tubealti;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (Field f : getClass().getDeclaredFields()) {
                Object value = f.get(this);
                if (value != null && !value.equals("NullKey")) {
                    stringBuilder.append(f.getName());
                    stringBuilder.append("=");
                    stringBuilder.append(value);
                    stringBuilder.append("&");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
