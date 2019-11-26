package com.shmedo.mcloudapp.entity.ble;

/**
 * 断线报警器
 */
public class BreakAlarmStatusSub {
    private int alarmStatus;

    public int getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(int alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    @Override
    public String toString() {
        return "BreakAlarmStatusSub{" +
                "alarmStatus='" + alarmStatus + '\'' +
                '}';
    }
}
