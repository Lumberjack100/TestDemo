package com.shmedo.core.model;

/**
 * Created by adu on 2017/12/15.
 * 本地时间的实体类
 */
public class LoaclTimeInfo {
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return this.time;
    }
}
