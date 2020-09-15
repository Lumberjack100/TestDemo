package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2017/12/15.
 * 系统运行状态实体类
 */
public class SystemRunStateInfo {
    private String gprsSignal;  //GPRS信号强度
    private String gpsNumber;   //GPS搜星数目
    private String systemStartUp;   //系统启动代码
    private String systemRestart;   //系统重启代码
    private String simCCID;         //SIM卡CCID
    private String internalTemperature; //设备内部温度
    private String batteryVoltage;      //设备电池电压
    private String externalVoltage;   //设备外部电压
    private String operator;   //运营商 CMCC：移动，UNICOM：联通，CT：电信
    private String networkMode;    //网络模式 0,2,3：2G，4,5,6：3G，7：4G，100：CDMA

    public String getGprsSignal() {
        return gprsSignal;
    }

    public void setGprsSignal(String gprsSignal) {
        this.gprsSignal = gprsSignal;
    }

    public String getGpsNumber() {
        return gpsNumber;
    }

    public void setGpsNumber(String gpsNumber) {
        this.gpsNumber = gpsNumber;
    }

    public String getSystemStartUp() {
        return systemStartUp;
    }

    public void setSystemStartUp(String systemStartUp) {
        this.systemStartUp = systemStartUp;
    }

    public String getSystemRestart() {
        return systemRestart;
    }

    public void setSystemRestart(String systemRestart) {
        this.systemRestart = systemRestart;
    }

    public String getSimCCID() {
        return simCCID;
    }

    public void setSimCCID(String simCCID) {
        this.simCCID = simCCID;
    }

    public String getInternalTemperature() {
        return internalTemperature;
    }

    public void setInternalTemperature(String internalTemperature) {
        this.internalTemperature = internalTemperature;
    }

    public String getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(String batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public String getExternalVoltage() {
        return externalVoltage;
    }

    public void setExternalVoltage(String externalVoltage) {
        this.externalVoltage = externalVoltage;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getNetworkMode() {
        return networkMode;
    }

    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }

    @Override
    public String toString() {
        return "SystemRunStateInfo{" +
                "gprsSignal='" + gprsSignal + '\'' +
                ", gpsNumber='" + gpsNumber + '\'' +
                ", systemStartUp='" + systemStartUp + '\'' +
                ", systemRestart='" + systemRestart + '\'' +
                ", simCCID='" + simCCID + '\'' +
                ", internalTemperature='" + internalTemperature + '\'' +
                ", batteryVoltage='" + batteryVoltage + '\'' +
                ", externalVoltage='" + externalVoltage + '\'' +
                ", operator='" + operator + '\'' +
                ", networkMode='" + networkMode + '\'' +
                '}';
    }
}
