package com.shmedo.core.cmd.entity;


import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.exception.DASParameterException;

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
