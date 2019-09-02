package com.shmedo.mcloudapp.entity.ble;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble
 * 文件名:   DigitalOsmometerFunctionSub
 * 创建者:   dpc
 * 创建时间:  2019/4/17 11:30
 * 描述：   开启/关闭 数字式渗压计功能
 */
public class DigitalOsmometerFunctionSub {
    private int osmometerStatus;


    public int getOsmometerStatus() {
        return osmometerStatus;
    }


    public void setOsmometerStatus(int osmometerStatus) {
        this.osmometerStatus = osmometerStatus;
    }


    @Override public String toString() {
        return "DigitalOsmometerFunctionSub{" +
            "osmometerStatus=" + osmometerStatus +
            '}';
    }
}
