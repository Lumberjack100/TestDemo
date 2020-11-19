package com.shmedo.configlibrary.iot.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/19/20 <br/>
 * 描述：    终端设备 Sn 号码
 */
public class TerminalSNEntity implements Validater {
    private String sn;

    public TerminalSNEntity(String sn) {
        this.sn = sn;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        return "sn=" + sn;
    }
}
