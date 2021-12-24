package com.shmedo.mcloudapp.deviceconfig.model.params;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/23 <br/>
 * 描述：     固件升级指令参数
 */
public class FirmwareCmdParam {
    private List<String> deviceTokenList;//设备SN号列表
    private int firmwareID;//产品固件编号

    public List<String> getDeviceTokenList() {
        return deviceTokenList;
    }

    public void setDeviceTokenList(List<String> deviceTokenList) {
        this.deviceTokenList = deviceTokenList;
    }

    public int getFirmwareID() {
        return firmwareID;
    }

    public void setFirmwareID(int firmwareID) {
        this.firmwareID = firmwareID;
    }
}
