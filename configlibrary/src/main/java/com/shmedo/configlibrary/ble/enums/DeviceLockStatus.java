package com.shmedo.configlibrary.ble.enums;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.common.enumerate
 * 文件名:   DeviceLockStatusInfo
 * 创建者:   dpc
 * 创建时间:  2019/4/26 09:01
 * 描述：    TODO #gh#
 */

public enum DeviceLockStatus {
    /**
     * 开启
     */
    UNLOCK(0),

    /**
     * 关闭
     */
    LOCK(1);

    private int status;

    DeviceLockStatus(int status) {
        this.status = status;
    }

    public static DeviceLockStatus valueOf(int status) {
        switch (status) {
            case 0:
                return UNLOCK;
            case 1:
                return LOCK;
            default:
                return UNLOCK;
        }
    }
}
