package com.shmedo.configlibrary.iot.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/23/20 <br/>
 * 描述：     获取Vms终端某个通道下传感器参数
 */
public class GetVmsTerminalSensorParamsEntity implements Validater {
    private String sn;
    private int channel;

    public GetVmsTerminalSensorParamsEntity(String sn, int channel) {
        this.sn = sn;
        this.channel = channel;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        return "sn=" + sn + "&channel=" + channel;
    }
}
