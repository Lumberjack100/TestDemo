package com.shmedo.configlibrary.iot.cmd.entity.e40;

import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/26 <br/>
 * 描述：      查询卫星信息
 */
public class SatelitteTypeEntity implements Validater {
    private String type;

    public SatelitteTypeEntity(String type) {
        this.type = type;
    }

    @Override
    public void validate() {
        if (!type.equals("ALL") && !type.equals("BDS") && !type.equals("GPS") && !type.equals("GLO"))
            throw new DASParameterException("卫星类别不存在");
    }

    @Override
    public String toString() {
        return "type=" + type;
    }
}
