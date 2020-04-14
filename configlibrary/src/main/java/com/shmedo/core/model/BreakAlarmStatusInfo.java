package com.shmedo.core.model;


import com.shmedo.core.enums.BreakAlarmStatus;

/**
 * 断线报警器实体类
 */
public class BreakAlarmStatusInfo {
    private BreakAlarmStatus status;



    public BreakAlarmStatus getStatus() {
        return status;
    }

    public void setStatus(BreakAlarmStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BreakAlarmStatusInfo{" +
                "status=" + status +
                '}';
    }
}
