package com.shmedo.core.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：   系统运行状态
 */
public class OperatorInfo {
    /**
     * $$014,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)\r\n
     * （1）信号强度
     * （2）GPS搜星数目
     * （3）启动代码1
     * （4）启动代码2
     * （5）SIM卡号
     * （6）MCU温度
     * （7）设备内部电压
     * （8）设备外部电压
     * （9）运营商类型，CMCC：移动，UNICOM：联通，CT：电信
     * （10）网络类型，0,2,3：2G，4,5,6：3G，7：4G，100：CDMA
     * 示例：
     * $$014,22,0,20,1,89860446091891274425,0.0,6.9,0.1,CMCC,7
     */
    private int signalStrength;
    private String GPSSearchStars;
    private String startupCode1;
    private String startupCode2;
    private String SIMCardNumber;
    private String MCUTemperature;
    private String deviceInternalVoltage;
    private String deviceExternalVoltage;
    private String operatorType;
    private String networkType;

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getGPSSearchStars() {
        return GPSSearchStars;
    }

    public void setGPSSearchStars(String GPSSearchStars) {
        this.GPSSearchStars = GPSSearchStars;
    }

    public String getStartupCode1() {
        return startupCode1;
    }

    public void setStartupCode1(String startupCode1) {
        this.startupCode1 = startupCode1;
    }

    public String getStartupCode2() {
        return startupCode2;
    }

    public void setStartupCode2(String startupCode2) {
        this.startupCode2 = startupCode2;
    }

    public String getSIMCardNumber() {
        return SIMCardNumber;
    }

    public void setSIMCardNumber(String SIMCardNumber) {
        this.SIMCardNumber = SIMCardNumber;
    }

    public String getMCUTemperature() {
        return MCUTemperature;
    }

    public void setMCUTemperature(String MCUTemperature) {
        this.MCUTemperature = MCUTemperature;
    }

    public String getDeviceInternalVoltage() {
        return deviceInternalVoltage;
    }

    public void setDeviceInternalVoltage(String deviceInternalVoltage) {
        this.deviceInternalVoltage = deviceInternalVoltage;
    }

    public String getDeviceExternalVoltage() {
        return deviceExternalVoltage;
    }

    public void setDeviceExternalVoltage(String deviceExternalVoltage) {
        this.deviceExternalVoltage = deviceExternalVoltage;
    }

    public String getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(String operatorType) {
        this.operatorType = operatorType;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    @Override
    public String toString() {
        return "OperatorInfo{" +
                "signalStrength=" + signalStrength +
                ", GPSSearchStars='" + GPSSearchStars + '\'' +
                ", startupCode1='" + startupCode1 + '\'' +
                ", startupCode2='" + startupCode2 + '\'' +
                ", SIMCardNumber='" + SIMCardNumber + '\'' +
                ", MCUTemperature='" + MCUTemperature + '\'' +
                ", deviceInternalVoltage='" + deviceInternalVoltage + '\'' +
                ", deviceExternalVoltage='" + deviceExternalVoltage + '\'' +
                ", operatorType='" + operatorType + '\'' +
                ", networkType='" + networkType + '\'' +
                '}';
    }
}
