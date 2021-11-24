package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.OsmometerStatus;

/**
 * Created by adu on 2018/1/8.
 * 开启/关闭 数字水位计功能实体类
 */
public class DigitalOsmometerFunctionInfo {
    private OsmometerStatus osmometerStatus;

    public OsmometerStatus getOsmometerStatus() {
        return osmometerStatus;
    }

    public void setOsmometerStatus(OsmometerStatus osmometerStatus) {
        this.osmometerStatus = osmometerStatus;
    }

    @Override
    public String toString() {
        return "DigitalOsmometerFunctionInfo{" +
                "osmometerStatus=" + osmometerStatus +
                '}';
    }
}
