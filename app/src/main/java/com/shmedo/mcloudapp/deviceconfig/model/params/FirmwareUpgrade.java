package com.shmedo.mcloudapp.deviceconfig.model.params;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/22/21 <br/>
 * 描述：     固件升级参数
 */
public class FirmwareUpgrade {
    private int companyID;//当前公司ID
    private int deviceID;//设备ID
    private int firmwareID;//固件ID

    public FirmwareUpgrade(int companyID, int deviceID, int firmwareID) {
        this.companyID = companyID;
        this.deviceID = deviceID;
        this.firmwareID = firmwareID;
    }

    public int getCompanyID() {
        return companyID;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public int getFirmwareID() {
        return firmwareID;
    }
}
