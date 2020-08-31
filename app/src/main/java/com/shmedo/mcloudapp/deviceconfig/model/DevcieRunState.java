package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：     设备运行状态信息
 */
public class DevcieRunState {

    /**
     * token : 110028L
     * time : 2020-08-31 11:24:46
     * extPowerVolt : 8.300000190734863
     * temp : 0.0
     * humidity : 0.0
     * fourGSignal : -51.0
     * bdSignal : 0.0
     * swVersion : 3.0.8
     * location : 106.71,26.57
     * sensorErrno : [{"errno":0,"sensor_id":0},{"errno":-3,"sensor_id":1},{"errno":-3,"sensor_id":2},{"errno":-3,"sensor_id":4}]
     * solarVolt : 23.899999618530273
     * batteryVolt : 0.0
     * supplyPower : 0.0
     * consumePower : 0.0
     */

    private String token;//设备SN号
    private String time;//时间
    private double extPowerVolt;//外接电源电压
    private double temp;//温度
    private double humidity;//湿度
    private double fourGSignal;//4G信号强度
    private double bdSignal;//北斗功率等级
    private String swVersion;//固件版本
    private String location;//位置
    private String sensorErrno;//传感器状态码
    private double solarVolt;//太阳能板电压
    private double batteryVolt;//蓄电池电压
    private double supplyPower;//补充功率
    private double consumePower;//消耗功率

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getExtPowerVolt() {
        return extPowerVolt;
    }

    public void setExtPowerVolt(double extPowerVolt) {
        this.extPowerVolt = extPowerVolt;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getFourGSignal() {
        return fourGSignal;
    }

    public void setFourGSignal(double fourGSignal) {
        this.fourGSignal = fourGSignal;
    }

    public double getBdSignal() {
        return bdSignal;
    }

    public void setBdSignal(double bdSignal) {
        this.bdSignal = bdSignal;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSensorErrno() {
        return sensorErrno;
    }

    public void setSensorErrno(String sensorErrno) {
        this.sensorErrno = sensorErrno;
    }

    public double getSolarVolt() {
        return solarVolt;
    }

    public void setSolarVolt(double solarVolt) {
        this.solarVolt = solarVolt;
    }

    public double getBatteryVolt() {
        return batteryVolt;
    }

    public void setBatteryVolt(double batteryVolt) {
        this.batteryVolt = batteryVolt;
    }

    public double getSupplyPower() {
        return supplyPower;
    }

    public void setSupplyPower(double supplyPower) {
        this.supplyPower = supplyPower;
    }

    public double getConsumePower() {
        return consumePower;
    }

    public void setConsumePower(double consumePower) {
        this.consumePower = consumePower;
    }
}
