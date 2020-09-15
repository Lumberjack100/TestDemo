package com.shmedo.configlibrary.ble.cmd.entity;


import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 断线报警器状态
 */
public class BreakAlarmStatusEntity  implements Validater {

    private int status;

    public BreakAlarmStatusEntity(int status) {
        this.status = status;
    }

    @Override
    public void validate() {
        if (status != 0 && status != 1 && status != 2)
            throw new DASParameterException("断线报警器状态错误");
    }

    @Override
    public String toString() {
        return String.valueOf(status);
    }
}
