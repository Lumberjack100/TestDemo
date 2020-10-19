package com.shmedo.configlibrary.ble.cmd.entity;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.cmd.entity <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：   设置渗压计绳长
 */
public class SetOsmometerCordLengthEntity implements Validater {
    private String cordLength;

    public SetOsmometerCordLengthEntity(String cordLength) {
        this.cordLength = cordLength;
    }

    @Override
    public void validate() {
//        if (cordLength > 65535)
//            throw new DASParameterException("绳长参数错误");
    }

    @Override
    public String toString() {
        return cordLength + "";
    }
}
