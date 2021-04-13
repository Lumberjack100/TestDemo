package com.shmedo.configlibrary.iot.cmd.entity.das;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  4/12/21 <br/>
 * 描述：    生成DAS  激活参数拼接指令
 */
public class DasActiveEntity implements Validater {
    private String mode;//0表示待机，1表示激活

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mode=" + mode);

        return stringBuilder.toString();
    }
}
