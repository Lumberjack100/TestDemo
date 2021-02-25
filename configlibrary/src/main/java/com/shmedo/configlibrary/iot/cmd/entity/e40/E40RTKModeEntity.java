package com.shmedo.configlibrary.iot.cmd.entity.e40;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/25/21 <br/>
 * 描述：      生成GNSS RTK参数拼接指令
 */
public class E40RTKModeEntity implements Validater {
    private String mode;//0表示基站，1表示移动站

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
