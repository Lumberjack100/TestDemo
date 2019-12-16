package com.shmedo.mcloudapp.entity.devicedetails;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.devicedetails
 * 创建者:   dpc
 * 创建时间:  2019-12-11
 * 描述：    设备版本信息
 */
public class DeviceVersionInfo {
    /**
     * （1）SN号
     * （2）固件版本
     * （3）生产日期
     * 示例：
     * $$040,150000L,DAS-LF-V3.0.2,170817
     */
    private String snNumber;
    private String firmwareVersion;
    private String productionDate;

    public String getSnNumber() {
        return snNumber;
    }

    public void setSnNumber(String snNumber) {
        this.snNumber = snNumber;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }

    @Override
    public String toString() {
        return "DeviceVersionInfo{" +
                "snNumber='" + snNumber + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", productionDate='" + productionDate + '\'' +
                '}';
    }
}
