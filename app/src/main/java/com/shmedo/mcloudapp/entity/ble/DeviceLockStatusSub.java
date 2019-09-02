package com.shmedo.mcloudapp.entity.ble;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble
 * 文件名:   DeviceLockStatusSub
 * 创建者:   dpc
 * 创建时间:  2019/4/23 17:24
 * 描述：    设备锁状态
 */
public class DeviceLockStatusSub {
    private int lockStatus;


    public int getLockStatus() {
        return lockStatus;
    }


    public void setLockStatus(int lockStatus) {
        this.lockStatus = lockStatus;
    }


    @Override public String toString() {
        return "DeviceLockStatusSub{" +
            "lockStatus='" + lockStatus + '\'' +
            '}';
    }
}
