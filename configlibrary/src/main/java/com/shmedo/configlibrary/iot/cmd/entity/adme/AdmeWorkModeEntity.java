package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/10/21 <br/>
 * 描述：       生成ADME 工作模式参数拼接指令
 */
public class AdmeWorkModeEntity implements Validater {
    private String workmode;//工作模式(0:常规测量模式，1:特定点位模式，2:静态测量模式，3:设备停用模式)

    public void setWorkmode(String workmode) {
        this.workmode = workmode;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("workmode=" + workmode);

        return stringBuilder.toString();
    }
}
