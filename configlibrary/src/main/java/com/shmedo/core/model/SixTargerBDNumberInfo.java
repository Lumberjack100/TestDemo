package com.shmedo.core.model;

/**
 * Created by adu on 2017/12/15.
 * 六位目标北斗卡号实体类
 */
public class SixTargerBDNumberInfo {
    private String sixNumber;

    public String getSixNumber() {
        return sixNumber;
    }

    public void setSixNumber(String sixNumber) {
        this.sixNumber = sixNumber;
    }

    @Override
    public String toString() {
        return sixNumber;
    }
}
