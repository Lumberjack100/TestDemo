package com.shmedo.core.enums;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.enums <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：   日志输出状态
 */
public enum LogOutputStatus {
    CLOSE(0), //关闭

    OPEN(1);//打开


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
