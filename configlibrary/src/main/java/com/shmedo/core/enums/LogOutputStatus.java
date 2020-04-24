package com.shmedo.core.enums;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.enums <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：    TODO
 */
public enum LogOutputStatus {
    CLOSE(0), //关闭

    OPEN(1);


    private int status;

    LogOutputStatus(int i) {
        this.status = i;
    }

    public int toInt() {
        return status;
    }

    public static LogOutputStatus valueOf(int status) {
        switch (status) {
            case 0:
                return CLOSE;

            case 1:
                return OPEN;

            default:
                return CLOSE;
        }
    }
}
