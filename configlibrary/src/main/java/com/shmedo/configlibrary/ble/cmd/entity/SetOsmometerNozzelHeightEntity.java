package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.cmd.entity <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：    设置渗压计管口高程
 */
public class SetOsmometerNozzelHeightEntity implements Validater {
    private double nozzelHeight;

    public SetOsmometerNozzelHeightEntity(double nozzelHeight) {
        this.nozzelHeight = nozzelHeight;
    }

    @Override
    public void validate() {
        if (nozzelHeight > 65535)
            throw new DASParameterException("管口高程参数错误");
    }

    @Override
    public String toString() {
        return nozzelHeight + "";
    }
}
