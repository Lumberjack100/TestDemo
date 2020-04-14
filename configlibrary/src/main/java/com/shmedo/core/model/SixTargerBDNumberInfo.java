package com.shmedo.core.model;

/**
 * Created by adu on 2017/12/15.
 * 六位目标北斗卡号实体类
 */
public class SixTargerBDNumberInfo {
    private int sixNumber;

    public int getSixNumber() {
        return sixNumber;
    }

    public void setSixNumber(int sixNumber) {
        this.sixNumber = sixNumber;
    }

    @Override
    public String toString() {
        return String.valueOf(this.sixNumber);
    }
}
