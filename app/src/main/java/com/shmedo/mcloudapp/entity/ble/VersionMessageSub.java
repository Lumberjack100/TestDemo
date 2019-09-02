package com.shmedo.mcloudapp.entity.ble;



/**
 * ProgramName:   das-config-app
 * PackageName:   com.example.medoDas.entity
 * Author:        社会小青年
 * Date:          2018/3/23 10:59
 * Description：获取版本信息
 */

public class VersionMessageSub {
    private String productID;       //产品序列号
    private String firmwareVersion; //固件版本号
    private String produceDate;     //生产日期

    //@JavascriptInterface
    public String getProductID() {
        return productID;
    }


    public void setProductID(String productID) {
        this.productID = productID;
    }

    //@JavascriptInterface
    public String getFirmwareVersion() {
        return firmwareVersion;
    }


    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    //@JavascriptInterface
    public String getProduceDate() {
        return produceDate;
    }


    public void setProduceDate(String produceDate) {
        this.produceDate = produceDate;
    }


    @Override public String toString() {
        return "VersionMessageSub{" +
            "productID='" + productID + '\'' +
            ", firmwareVersion='" + firmwareVersion + '\'' +
            ", produceDate='" + produceDate + '\'' +
            '}';
    }
}
