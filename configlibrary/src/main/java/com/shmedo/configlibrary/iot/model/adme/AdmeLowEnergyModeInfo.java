package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/10/21 <br/>
 * 描述：      ADME 低功耗模式
 */
public class AdmeLowEnergyModeInfo {
    private String mode;//工作模式(0:正常模式，1:低功耗模式)

    public String getMode() {
        return TextUtils.isEmpty(mode) ? "" : mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
