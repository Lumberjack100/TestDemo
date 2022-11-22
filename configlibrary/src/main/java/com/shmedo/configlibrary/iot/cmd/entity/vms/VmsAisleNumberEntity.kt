package com.shmedo.configlibrary.iot.cmd.entity.vms;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：      Vms网关通道编号参数
 */
public class VmsAisleNumberEntity implements Validater {
    private int number;

    public VmsAisleNumberEntity(int number) {
        this.number = number;
    }

    @Override
    public void validate() {
        if (number != 0 && number != 1 && number != 2)
            throw new DASParameterException("Vms网关通道编号不存在");
    }

    @Override
    public String toString() {
        return "channel=" + number;
    }
}
