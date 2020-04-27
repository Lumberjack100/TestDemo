package com.shmedo.core.cmd.entity;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.utils.ValidateUtil;

/**
 * Created by adu on 2017/12/20.
 * 设置六位目标北斗卡号参数
 */
public class SixTargerBDNumberEntity implements Validater {
    private String sixNumber;

    public SixTargerBDNumberEntity(String sixNumber) {
        this.sixNumber = sixNumber;
    }

    @Override
    public void validate() {
        if (!ValidateUtil.isNumberSix(sixNumber))
            throw new DASParameterException("北斗卡号参数错误");
    }

    @Override
    public String toString() {
        return sixNumber;
    }
}
