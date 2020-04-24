package com.shmedo.core.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    设备网络状态
 */
public class DeviceNetStatus {
    /**
     * $$044n,(1),(2),(3),(4),(5),(6),(7),(8),(9)\r\n
     * 注：n取值1，2，3
     * （1）已发送数据
     * （2）已生成数据
     * （3）flash使能，取值0,1，1表示使能，0表示未使能
     * （4）flash读指针
     * （5）flash写指针
     * （6）中心使能：取值0,1，1表示使能，0表示未使能
     * （7）中心状态：取值0,1，1表示上线，0表示未下线
     * （8）4G模块状态：
     * （9）MQTT状态：
     *  (10)在线率，单位%
     * 示例：
     * $$0441,7,7,1,0x001000D4,0x001000D4,1,1,4,7
     */
    private String linkNumber;
    private String sentData;
    private String generatedData;
    private String flashEnable;
    private String flashReadPointer;
    private String flashWritePointer;
    private String linkEnable;
    private String linkStatus;
    private String FourGModuleStatus;
    private String mqttStatus;
    private String onlineRate;

    public String getLinkNumber() {
        return linkNumber;
    }

    public void setLinkNumber(String linkNumber) {
        this.linkNumber = linkNumber;
    }

    public String getSentData() {
        return sentData;
    }

    public void setSentData(String sentData) {
        this.sentData = sentData;
    }

    public String getGeneratedData() {
        return generatedData;
    }

    public void setGeneratedData(String generatedData) {
        this.generatedData = generatedData;
    }

    public String getFlashEnable() {
        return flashEnable;
    }

    public void setFlashEnable(String flashEnable) {
        this.flashEnable = flashEnable;
    }

    public String getFlashReadPointer() {
        return flashReadPointer;
    }

    public void setFlashReadPointer(String flashReadPointer) {
        this.flashReadPointer = flashReadPointer;
    }

    public String getFlashWritePointer() {
        return flashWritePointer;
    }

    public void setFlashWritePointer(String flashWritePointer) {
        this.flashWritePointer = flashWritePointer;
    }

    public String getLinkEnable() {
        return linkEnable;
    }

    public void setLinkEnable(String linkEnable) {
        this.linkEnable = linkEnable;
    }

    public String getLinkStatus() {
        return linkStatus;
    }

    public void setLinkStatus(String linkStatus) {
        this.linkStatus = linkStatus;
    }

    public String getFourGModuleStatus() {
        return FourGModuleStatus;
    }

    public void setFourGModuleStatus(String fourGModuleStatus) {
        FourGModuleStatus = fourGModuleStatus;
    }

    public String getMqttStatus() {
        return mqttStatus;
    }

    public void setMqttStatus(String mqttStatus) {
        this.mqttStatus = mqttStatus;
    }

    public String getOnlineRate() {
        return onlineRate;
    }

    public void setOnlineRate(String onlineRate) {
        this.onlineRate = onlineRate;
    }

    @Override
    public String toString() {
        return "DeviceNetStatus{" +
                "linkNumber='" + linkNumber + '\'' +
                ", sentData='" + sentData + '\'' +
                ", generatedData='" + generatedData + '\'' +
                ", flashEnable='" + flashEnable + '\'' +
                ", flashReadPointer='" + flashReadPointer + '\'' +
                ", flashWritePointer='" + flashWritePointer + '\'' +
                ", linkEnable='" + linkEnable + '\'' +
                ", linkStatus='" + linkStatus + '\'' +
                ", FourGModuleStatus='" + FourGModuleStatus + '\'' +
                ", mqttStatus='" + mqttStatus + '\'' +
                ", onlineRate='" + onlineRate + '\'' +
                '}';
    }
}
