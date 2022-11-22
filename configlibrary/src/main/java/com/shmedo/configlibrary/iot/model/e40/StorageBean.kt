package com.shmedo.configlibrary.iot.model.e40;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     TODO
 */
public class StorageBean {
    private String ram;
    private String flash;
    private String tfcard;

    public String getRam() {
        return TextUtils.isEmpty(ram) ? "" : ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getFlash() {
        return TextUtils.isEmpty(flash) ? "" : flash;
    }

    public void setFlash(String flash) {
        this.flash = flash;
    }

    public String getTfcard() {
        return TextUtils.isEmpty(tfcard) ? "" : tfcard;
    }

    public void setTfcard(String tfcard) {
        this.tfcard = tfcard;
    }
}
