package com.shmedo.configlibrary.iot.cmd.entity.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/1/25 <br/>
 * 描述：    ADME 拟人运动控制
 */
public class AdmeAnthropomorphicMovementEntity implements Validater {
    private String mode;

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public void validate() {
        if (TextUtils.isEmpty(mode))
            throw new DASParameterException("ADME 拟人运动使能参数不正确");
    }

    @Override
    public String toString() {
        return "mode=" + mode;
    }
}
