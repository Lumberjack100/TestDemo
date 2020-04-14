package com.shmedo.core.enums;

/**
 * 断线报警器状态
 */
public enum  BreakAlarmStatus {

    QUERY(0),OPEN(1), CLOSE(2);

    private int status;
    BreakAlarmStatus(int status) {
        this.status = status;
    }
    public int toInt() {
        return status;
    }
    public static BreakAlarmStatus valueOf(int status) {
        switch (status) {
            case 0: return QUERY;
            case 1: return OPEN;
            case 2: return CLOSE;
            default: return OPEN;
        }
    }
}
