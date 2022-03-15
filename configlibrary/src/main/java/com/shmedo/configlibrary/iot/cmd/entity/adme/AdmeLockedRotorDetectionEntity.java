package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/2/21 <br/>
 * 描述：     生成ADME 电机运动堵转缓停参数拼接指令
 */
public class AdmeLockedRotorDetectionEntity implements Validater {
    private String lowtbtss;//下放堵转缓停（0:关闭，1:开启）
    private String numpput;//单位时间脉冲数
    private String pdajtime;//脉冲检测判断时间
    private String detintiona;//堵转检测区间起始值
    private String detintionb;//堵转检测区间终值
    private String lowtorblothr;//下放力矩堵转阈值
    private String lowtordetime;//下放力矩检测判断时间
    private String lowsusrana;//下放缓停区间起始值
    private String lowsusranb;//下放缓停区间终值
    private String uptbtss;//上拉堵转缓停（0:关闭，1:开启）
    private String uptorblothr;//上拉力矩堵转阈值
    private String uptordetime;//上拉力矩检测判断时间
    private String upsusrana;//上拉缓停区间起始值
    private String upsusranb;//上拉缓停区间终值

    public void setLowtbtss(String lowtbtss) {
        this.lowtbtss = lowtbtss;
    }

    public void setNumpput(String numpput) {
        this.numpput = numpput;
    }

    public void setPdajtime(String pdajtime) {
        this.pdajtime = pdajtime;
    }

    public void setDetintiona(String detintiona) {
        this.detintiona = detintiona;
    }

    public void setDetintionb(String detintionb) {
        this.detintionb = detintionb;
    }

    public void setLowtorblothr(String lowtorblothr) {
        this.lowtorblothr = lowtorblothr;
    }

    public void setLowtordetime(String lowtordetime) {
        this.lowtordetime = lowtordetime;
    }

    public void setLowsusrana(String lowsusrana) {
        this.lowsusrana = lowsusrana;
    }

    public void setLowsusranb(String lowsusranb) {
        this.lowsusranb = lowsusranb;
    }

    public void setUptbtss(String uptbtss) {
        this.uptbtss = uptbtss;
    }

    public void setUptorblothr(String uptorblothr) {
        this.uptorblothr = uptorblothr;
    }

    public void setUptordetime(String uptordetime) {
        this.uptordetime = uptordetime;
    }

    public void setUpsusrana(String upsusrana) {
        this.upsusrana = upsusrana;
    }

    public void setUpsusranb(String upsusranb) {
        this.upsusranb = upsusranb;
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
