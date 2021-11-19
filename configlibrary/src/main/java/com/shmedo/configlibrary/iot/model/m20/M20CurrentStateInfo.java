package com.shmedo.configlibrary.iot.model.m20;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/19/21 <br/>
 * 描述：   M20设备当前状态
 */
public class M20CurrentStateInfo {
    /**
     * ext_power_volt : 0
     * inner_power_volt : 13.21
     * temp : 37.46
     * humidity : 0
     * temp_out : 0
     * humidity_out : 0
     * 4g_signal : -29
     * bd_signal : 0
     * sw_version : 1.3.99
     * sensor_errno : [{"errno":0,"sensor_id":"203_1"}]
     * location : 0.00000000E,0.00000000N
     * IMEI : 867435053801965
     * CCID : 89860445101970723723
     * solar_volt : 0
     * battery_volt : 0
     * supply_power : 0
     * consume_power : 0
     * work_current : 0
     * volt_percent : 0
     * Z_Angle : -86.15
     * SN : M2020C002V
     * eMMC Free : 13257MB
     * dataCenter3 : 1
     * dataCenter4 : 0
     * starNum : 0
     * gpsCard : S22221K726,391TN-2.059-1
     * self_check : GPS:1,eMMC:1,4g:1,RTC:1,solar485:0,G-Sensor:1
     */

    private String ext_power_volt;//外接电源电压
    private String inner_power_volt;//内部电源电压
    private String temp;//设备内部环境温度，单位摄氏度
    private String humidity;//设备内部湿度，单位 RH%
    private String temp_out;//设备外部环境温度，单位摄氏度
    private String humidity_out;//设备外部环境湿度，单位RH%
    @SerializedName("4g_signal")
    private String _$4g_signal;//4g信号强度
    private String bd_signal;//北斗信号强度
    private String sw_version;//固件版本
    private String location;//设备位置-经纬度，经度在前,纬度在后。E表示东经，W表示西经，N表示北纬，S表示南纬。
    private String IMEI;//IMEI卡号
    private String CCID;//物联网卡号
    private String solar_volt;//太阳能板电压,单位V
    private String battery_volt;//蓄电池电压，单位V
    private String supply_power;//近12小时补充功率，单位千瓦时
    private String consume_power;//近12小时消耗功率，单位千瓦时
    private String work_current;//设备工作电流，单位A
    private String volt_percent;
    private String X_Angle;//X倾角
    private String Y_Angle;//Y倾角
    private String Z_Angle;//Z倾角
    private String SN;//设备SN号
    @SerializedName("eMMC Free")
    private String eMMCFree;//存储状态
    private String dataCenter3;//数据中心3
    private String dataCenter4;//数据中心4
    private String starNum;//星数
    private String gpsCard;//板卡
    private String self_check;//设备自检
    private List<SensorErrnoBean> sensor_errno;

    public String getExt_power_volt() {
        return TextUtils.isEmpty(ext_power_volt) ? "" : ext_power_volt;
    }

    public void setExt_power_volt(String ext_power_volt) {
        this.ext_power_volt = ext_power_volt;
    }

    public String getInner_power_volt() {
        return TextUtils.isEmpty(inner_power_volt) ? "" : inner_power_volt;
    }

    public void setInner_power_volt(String inner_power_volt) {
        this.inner_power_volt = inner_power_volt;
    }

    public String getTemp() {
        return TextUtils.isEmpty(temp) ? "" : temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getHumidity() {
        return TextUtils.isEmpty(humidity) ? "" : humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTemp_out() {
        return TextUtils.isEmpty(temp_out) ? "" : temp_out;
    }

    public void setTemp_out(String temp_out) {
        this.temp_out = temp_out;
    }

    public String getHumidity_out() {
        return TextUtils.isEmpty(humidity_out) ? "" : humidity_out;
    }

    public void setHumidity_out(String humidity_out) {
        this.humidity_out = humidity_out;
    }

    public String get_$4g_signal() {
        return TextUtils.isEmpty(_$4g_signal) ? "" : _$4g_signal;
    }

    public void set_$4g_signal(String _$4g_signal) {
        this._$4g_signal = _$4g_signal;
    }

    public String getBd_signal() {
        return TextUtils.isEmpty(bd_signal) ? "" : bd_signal;
    }

    public void setBd_signal(String bd_signal) {
        this.bd_signal = bd_signal;
    }

    public String getSw_version() {
        return TextUtils.isEmpty(sw_version) ? "" : sw_version;
    }

    public void setSw_version(String sw_version) {
        this.sw_version = sw_version;
    }

    public String getLocation() {
        return TextUtils.isEmpty(location) ? "" : location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIMEI() {
        return TextUtils.isEmpty(IMEI) ? "" : IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getCCID() {
        return TextUtils.isEmpty(CCID) ? "" : CCID;
    }

    public void setCCID(String CCID) {
        this.CCID = CCID;
    }

    public String getSolar_volt() {
        return TextUtils.isEmpty(solar_volt) ? "" : solar_volt;
    }

    public void setSolar_volt(String solar_volt) {
        this.solar_volt = solar_volt;
    }

    public String getBattery_volt() {
        return TextUtils.isEmpty(battery_volt) ? "" : battery_volt;
    }

    public void setBattery_volt(String battery_volt) {
        this.battery_volt = battery_volt;
    }

    public String getSupply_power() {
        return TextUtils.isEmpty(supply_power) ? "" : supply_power;
    }

    public void setSupply_power(String supply_power) {
        this.supply_power = supply_power;
    }

    public String getConsume_power() {
        return TextUtils.isEmpty(consume_power) ? "" : consume_power;
    }

    public void setConsume_power(String consume_power) {
        this.consume_power = consume_power;
    }

    public String getWork_current() {
        return TextUtils.isEmpty(work_current) ? "" : work_current;
    }

    public void setWork_current(String work_current) {
        this.work_current = work_current;
    }

    public String getVolt_percent() {
        return TextUtils.isEmpty(volt_percent) ? "" : volt_percent;
    }

    public void setVolt_percent(String volt_percent) {
        this.volt_percent = volt_percent;
    }

    public String getX_Angle() {
        return TextUtils.isEmpty(X_Angle) ? "" : X_Angle;
    }

    public void setX_Angle(String x_Angle) {
        X_Angle = x_Angle;
    }

    public String getY_Angle() {
        return TextUtils.isEmpty(Y_Angle) ? "" : Y_Angle;
    }

    public void setY_Angle(String y_Angle) {
        Y_Angle = y_Angle;
    }

    public String getZ_Angle() {
        return TextUtils.isEmpty(Z_Angle) ? "" : Z_Angle;
    }

    public void setZ_Angle(String Z_Angle) {
        this.Z_Angle = Z_Angle;
    }

    public String getSN() {
        return TextUtils.isEmpty(SN) ? "" : SN;
    }

    public void setSN(String SN) {
        this.SN = SN;
    }

    public String getEMMCFree() {
        return TextUtils.isEmpty(eMMCFree) ? "" : eMMCFree;
    }

    public void setEMMCFree(String eMMCFree) {
        this.eMMCFree = eMMCFree;
    }

    public String getDataCenter3() {
        return TextUtils.isEmpty(dataCenter3) ? "" : dataCenter3;
    }

    public void setDataCenter3(String dataCenter3) {
        this.dataCenter3 = dataCenter3;
    }

    public String getDataCenter4() {
        return TextUtils.isEmpty(dataCenter4) ? "" : dataCenter4;
    }

    public void setDataCenter4(String dataCenter4) {
        this.dataCenter4 = dataCenter4;
    }

    public String getStarNum() {
        return TextUtils.isEmpty(starNum) ? "" : starNum;
    }

    public void setStarNum(String starNum) {
        this.starNum = starNum;
    }

    public String getGpsCard() {
        return TextUtils.isEmpty(gpsCard) ? "" : gpsCard;
    }

    public void setGpsCard(String gpsCard) {
        this.gpsCard = gpsCard;
    }

    public String getSelf_check() {
        return TextUtils.isEmpty(self_check) ? "" : self_check;
    }

    public void setSelf_check(String self_check) {
        this.self_check = self_check;
    }

    public List<SensorErrnoBean> getSensor_errno() {
        return sensor_errno == null ? new ArrayList<SensorErrnoBean>() : sensor_errno;
    }

    public void setSensor_errno(List<SensorErrnoBean> sensor_errno) {
        this.sensor_errno = sensor_errno;
    }
}
