package com.shmedo.configlibrary.ble.model;

/**
 * Created by adu on 2017/12/14.
 *  版本信息实体类
 */
public class VersionMessageInfo {
    /**
     * （1）SN号
     * （2）固件版本
     * （3）生产日期
     * 示例：
     * $$040,150000L,DAS-LF-V3.0.2,170817
     */
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
