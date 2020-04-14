package com.shmedo.core.model;

/**
 * Created by adu on 2017/12/14.
 *  版本信息实体类
 */
public class VersionMessageInfo {

    private String productID;       //产品序列号
    private String firmwareVersion; //固件版本号
    private String produceDate;     //生产日期

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getProduceDate() {
        return produceDate;
    }

    public void setProduceDate(String produceDate) {
        this.produceDate = produceDate;
    }

    @Override
    public String toString() {
        return "VersionMessageInfo{" +
                "productID='" + productID + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", produceDate='" + produceDate + '\'' +
                '}';
    }
}
