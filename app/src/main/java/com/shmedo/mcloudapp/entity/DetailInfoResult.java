package com.shmedo.mcloudapp.entity;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   DetailInfoResult
 * 创建者:   dpc
 * 创建时间:  2019/3/28 11:31
 * 描述：    TODO
 */
public class DetailInfoResult {
    private String productionTime;//生产日期
    private String softwareVersion;//软件版本
    private String hardwareVersion;//硬件版本
    private String productionPurpose;//生产目的
    private String ccID1;//ccid1
    private String sim1NO;//sim卡1
    private String sim1State;//sim卡1状态
    private Integer sim1Data;//sim卡1流量剩余，单位MB
    private String sim1Vendor;//sim卡1厂家
    private String sim1UpdateTime;//sim卡1流量获取时间
    private String ccID2;
    private String sim2NO;
    private String sim2State;
    private Integer sim2Data;
    private String sim2Vendor;
    private String sim2UpdateTime;
    private String beidouNO;    //北斗卡号
    private String exValues;    //扩展属性


    public String getProductionTime() {
        return productionTime;
    }


    public void setProductionTime(String productionTime) {
        this.productionTime = productionTime;
    }


    public String getSoftwareVersion() {
        return softwareVersion;
    }


    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }


    public String getHardwareVersion() {
        return hardwareVersion;
    }


    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }


    public String getProductionPurpose() {
        return productionPurpose;
    }


    public void setProductionPurpose(String productionPurpose) {
        this.productionPurpose = productionPurpose;
    }


    public String getCcID1() {
        return ccID1;
    }


    public void setCcID1(String ccID1) {
        this.ccID1 = ccID1;
    }


    public String getSim1NO() {
        return sim1NO;
    }


    public void setSim1NO(String sim1NO) {
        this.sim1NO = sim1NO;
    }


    public String getSim1State() {
        return sim1State;
    }


    public void setSim1State(String sim1State) {
        this.sim1State = sim1State;
    }


    public Integer getSim1Data() {
        return sim1Data;
    }


    public void setSim1Data(Integer sim1Data) {
        this.sim1Data = sim1Data;
    }


    public String getSim1Vendor() {
        return sim1Vendor;
    }


    public void setSim1Vendor(String sim1Vendor) {
        this.sim1Vendor = sim1Vendor;
    }


    public String getSim1UpdateTime() {
        return sim1UpdateTime;
    }


    public void setSim1UpdateTime(String sim1UpdateTime) {
        this.sim1UpdateTime = sim1UpdateTime;
    }


    public String getCcID2() {
        return ccID2;
    }


    public void setCcID2(String ccID2) {
        this.ccID2 = ccID2;
    }


    public String getSim2NO() {
        return sim2NO;
    }


    public void setSim2NO(String sim2NO) {
        this.sim2NO = sim2NO;
    }


    public String getSim2State() {
        return sim2State;
    }


    public void setSim2State(String sim2State) {
        this.sim2State = sim2State;
    }


    public Integer getSim2Data() {
        return sim2Data;
    }


    public void setSim2Data(Integer sim2Data) {
        this.sim2Data = sim2Data;
    }


    public String getSim2Vendor() {
        return sim2Vendor;
    }


    public void setSim2Vendor(String sim2Vendor) {
        this.sim2Vendor = sim2Vendor;
    }


    public String getSim2UpdateTime() {
        return sim2UpdateTime;
    }


    public void setSim2UpdateTime(String sim2UpdateTime) {
        this.sim2UpdateTime = sim2UpdateTime;
    }


    public String getBeidouNO() {
        return beidouNO;
    }


    public void setBeidouNO(String beidouNO) {
        this.beidouNO = beidouNO;
    }


    public String getExValues() {
        return exValues;
    }


    public void setExValues(String exValues) {
        this.exValues = exValues;
    }
}
