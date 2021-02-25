package com.shmedo.configlibrary.iot.model.e40;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/25/21 <br/>
 * 描述：     GNSS RTK 模式
 */
public class E40RTKModeInfo {
    private String mode;//0表示基站，1表示移动站

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
