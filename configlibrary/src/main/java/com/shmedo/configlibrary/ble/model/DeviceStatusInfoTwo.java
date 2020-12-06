package com.shmedo.configlibrary.ble.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：   设备状态信息实体类
 */
public class DeviceStatusInfoTwo {
    /**
     * $$042,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(15),(16),(17),(18)\r\n
     * （1）SN号  150000L
     * （2）经度  0.000000
     * （3）纬度  0.000000
     * （4）设备内部电压   8.4
     * （5）设备外部电压   11.9
     * （6）太阳能控制器状态  0
     * （7）太阳能板电压   1.1
     * （8）电池电压   11.9
     * （9）日发电量   0.0
     * （10）日耗电量    0.0
     * （11）机箱内部温湿度状态   0
     * （12）机箱内部温度   23.1
     * （13）机箱内部湿度    35.9
     * （14）机箱外部温湿度状态  0
     * （15）机箱外部温度    23.8
     * （16）机箱外部湿度   35.1
     * （17）开关量类型，1：雨量计，2：关闭，3：断线报警器
     * （18）降雨量或断线报警器状态(1:断开，0：闭合)
     * 示例：
     * $$042,150000L,0.000000,0.000000,8.4,11.9,0,1.1,11.9,0.0,0.0,0,23.1,35.9,0,23.8,35.1,2,0.0
     */
    private String snNumber;
    private String longitude;
    private String latitude;
    private double internalVoltage;
    private String externalVoltage;
    private String solarControllerStatus;
    private String solarPanelVoltage;
    private String batteryVoltage;
    private String dailyPowerGeneration;
    private String dailyPowerConsumption;
    private String internalTempHumidityStatus;
    private String internalTemperature;
    private String internalHumidity;
    private String externalTempHumidityStatus;
    private String externalTemperature;
    private String externalHumidity;
    private String switchType;
    private String rainfallStatus;

    public String getSnNumber() {
        return TextUtils.isEmpty(snNumber) ? "" : snNumber;
    }

    public void setSnNumber(String snNumber) {
        this.snNumber = snNumber;
    }

    public String getLongitude() {
        return TextUtils.isEmpty(longitude) ? "" : longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return TextUtils.isEmpty(latitude) ? "" : latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public double getInternalVoltage() {
        return internalVoltage;
    }

    public void setInternalVoltage(double internalVoltage) {
        this.internalVoltage = internalVoltage;
    }

    public String getExternalVoltage() {
        return TextUtils.isEmpty(externalVoltage) ? "" : externalVoltage;
    }

    public void setExternalVoltage(String externalVoltage) {
        this.externalVoltage = externalVoltage;
    }

    public String getSolarControllerStatus() {
        return TextUtils.isEmpty(solarControllerStatus) ? "" : solarControllerStatus;
    }

    public void setSolarControllerStatus(String solarControllerStatus) {
        this.solarControllerStatus = solarControllerStatus;
    }

    public String getSolarPanelVoltage() {
        return TextUtils.isEmpty(solarPanelVoltage) ? "" : solarPanelVoltage;
    }

    public void setSolarPanelVoltage(String solarPanelVoltage) {
        this.solarPanelVoltage = solarPanelVoltage;
    }

    public String getBatteryVoltage() {
        return TextUtils.isEmpty(batteryVoltage) ? "" : batteryVoltage;
    }

    public void setBatteryVoltage(String batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public String getDailyPowerGeneration() {
        return TextUtils.isEmpty(dailyPowerGeneration) ? "" : dailyPowerGeneration;
    }

    public void setDailyPowerGeneration(String dailyPowerGeneration) {
        this.dailyPowerGeneration = dailyPowerGeneration;
    }

    public String getDailyPowerConsumption() {
        return TextUtils.isEmpty(dailyPowerConsumption) ? "" : dailyPowerConsumption;
    }

    public void setDailyPowerConsumption(String dailyPowerConsumption) {
        this.dailyPowerConsumption = dailyPowerConsumption;
    }

    public String getInternalTempHumidityStatus() {
        return TextUtils.isEmpty(internalTempHumidityStatus) ? "" : internalTempHumidityStatus;
    }

    public void setInternalTempHumidityStatus(String internalTempHumidityStatus) {
        this.internalTempHumidityStatus = internalTempHumidityStatus;
    }

    public String getInternalTemperature() {
        return TextUtils.isEmpty(internalTemperature) ? "" : internalTemperature;
    }

    public void setInternalTemperature(String internalTemperature) {
        this.internalTemperature = internalTemperature;
    }

    public String getInternalHumidity() {
        return TextUtils.isEmpty(internalHumidity) ? "" : internalHumidity;
    }

    public void setInternalHumidity(String internalHumidity) {
        this.internalHumidity = internalHumidity;
    }

    public String getExternalTempHumidityStatus() {
        return TextUtils.isEmpty(externalTempHumidityStatus) ? "" : externalTempHumidityStatus;
    }

    public void setExternalTempHumidityStatus(String externalTempHumidityStatus) {
        this.externalTempHumidityStatus = externalTempHumidityStatus;
    }

    public String getExternalTemperature() {
        return TextUtils.isEmpty(externalTemperature) ? "" : externalTemperature;
    }

    public void setExternalTemperature(String externalTemperature) {
        this.externalTemperature = externalTemperature;
    }

    public String getExternalHumidity() {
        return TextUtils.isEmpty(externalHumidity) ? "" : externalHumidity;
    }

    public void setExternalHumidity(String externalHumidity) {
        this.externalHumidity = externalHumidity;
    }

    public String getSwitchType() {
        return TextUtils.isEmpty(switchType) ? "" : switchType;
    }

    public void setSwitchType(String switchType) {
        this.switchType = switchType;
    }

    public String getRainfallStatus() {
        return TextUtils.isEmpty(rainfallStatus) ? "" : rainfallStatus;
    }

    public void setRainfallStatus(String rainfallStatus) {
        this.rainfallStatus = rainfallStatus;
    }

    @Override
    public String toString() {
        return "DeviceStatusInfoTwo{" +
                "snNumber='" + snNumber + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", internalVoltage=" + internalVoltage +
                ", externalVoltage='" + externalVoltage + '\'' +
                ", solarControllerStatus='" + solarControllerStatus + '\'' +
                ", solarPanelVoltage='" + solarPanelVoltage + '\'' +
                ", batteryVoltage='" + batteryVoltage + '\'' +
                ", dailyPowerGeneration='" + dailyPowerGeneration + '\'' +
                ", dailyPowerConsumption='" + dailyPowerConsumption + '\'' +
                ", internalTempHumidityStatus='" + internalTempHumidityStatus + '\'' +
                ", internalTemperature='" + internalTemperature + '\'' +
                ", internalHumidity='" + internalHumidity + '\'' +
                ", externalTempHumidityStatus='" + externalTempHumidityStatus + '\'' +
                ", externalTemperature='" + externalTemperature + '\'' +
                ", externalHumidity='" + externalHumidity + '\'' +
                ", switchType='" + switchType + '\'' +
                ", rainfallStatus='" + rainfallStatus + '\'' +
                '}';
    }
}
