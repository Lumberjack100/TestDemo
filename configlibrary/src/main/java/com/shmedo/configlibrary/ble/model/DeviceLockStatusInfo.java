package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.DeviceLockStatus;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common
 * 文件名:   DeviceLockStatusInfo
 * 创建者:   dpc
 * 创建时间:  2019/4/26 09:14
 * 描述：    设备锁定状态实体类
 */

public class DeviceLockStatusInfo {
    private DeviceLockStatus status;


    public DeviceLockStatus getStatus() {
        return status;
    }


    public void setStatus(DeviceLockStatus status) {
        this.status = status;
    }


    @Override public String toString() {
        return "DeviceLockStatusInfo{" +
            "status=" + status +
            '}';
    }
}
