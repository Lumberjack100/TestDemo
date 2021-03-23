package com.shmedo.configlibrary.iot.cmd.entity.vms;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/23/21 <br/>
 * 描述：    Vms 挂载终端的运行情况
 */
public class VmsTerminalStatusEntity implements Validater {
    private int channelNumber;
    private int index;

    public VmsTerminalStatusEntity(int channelNumber, int index) {
        this.channelNumber = channelNumber;
        this.index = index;
    }

    @Override
    public void validate() {
        if (channelNumber != 0 && channelNumber != 1 && channelNumber != 2)
            throw new DASParameterException("Vms网关通道编号不存在");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("channel=" + channelNumber);
        stringBuilder.append("&");

        stringBuilder.append("index=" + index);

        return stringBuilder.toString();
    }
}
