package com.shmedo.configlibrary.iot.model.rn20;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/11/22 <br/>
 * 描述：     TODO
 */
public class Rn20ModuleStatus {
    private String sn;//SN号
    private String flash;//存储芯片
    private String ads;//电压采集
    private String ble;//蓝牙
    private String lora;//Lora模块
    private String vm501;//振弦采集模块
    private String adxl362;//加速度模块
    private String mmc5883;//方位角模块
    private String scl3300;//倾角模块
    private String aht21;//温湿度模块
    private String rtc;//rtc时钟
    private String ltc2945;//电流检测模块

    public String getSn() {
        return TextUtils.isEmpty(sn) ? "" : sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getFlash() {
        return TextUtils.isEmpty(flash) ? "" : flash;
    }

    public void setFlash(String flash) {
        this.flash = flash;
    }

    public String getAds() {
        return TextUtils.isEmpty(ads) ? "" : ads;
    }

    public void setAds(String ads) {
        this.ads = ads;
    }

    public String getBle() {
        return TextUtils.isEmpty(ble) ? "" : ble;
    }

    public void setBle(String ble) {
        this.ble = ble;
    }

    public String getLora() {
        return TextUtils.isEmpty(lora) ? "" : lora;
    }

    public void setLora(String lora) {
        this.lora = lora;
    }

    public String getVm501() {
        return TextUtils.isEmpty(vm501) ? "" : vm501;
    }

    public void setVm501(String vm501) {
        this.vm501 = vm501;
    }

    public String getAdxl362() {
        return TextUtils.isEmpty(adxl362) ? "" : adxl362;
    }

    public void setAdxl362(String adxl362) {
        this.adxl362 = adxl362;
    }

    public String getMmc5883() {
        return TextUtils.isEmpty(mmc5883) ? "" : mmc5883;
    }

    public void setMmc5883(String mmc5883) {
        this.mmc5883 = mmc5883;
    }

    public String getScl3300() {
        return TextUtils.isEmpty(scl3300) ? "" : scl3300;
    }

    public void setScl3300(String scl3300) {
        this.scl3300 = scl3300;
    }

    public String getAht21() {
        return TextUtils.isEmpty(aht21) ? "" : aht21;
    }

    public void setAht21(String aht21) {
        this.aht21 = aht21;
    }

    public String getRtc() {
        return TextUtils.isEmpty(rtc) ? "" : rtc;
    }

    public void setRtc(String rtc) {
        this.rtc = rtc;
    }

    public String getLtc2945() {
        return TextUtils.isEmpty(ltc2945) ? "" : ltc2945;
    }

    public void setLtc2945(String ltc2945) {
        this.ltc2945 = ltc2945;
    }
}
