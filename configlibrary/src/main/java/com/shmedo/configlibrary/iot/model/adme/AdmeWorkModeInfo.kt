package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/10/21 <br/>
 * 描述：      ADME 工作模式
 */
public class AdmeWorkModeInfo {
    private String workmode;//工作模式(0:常规测量模式，1:特定点位模式，2:静态测量模式，3:设备停用模式)

    public String getWorkmode() {
        return TextUtils.isEmpty(workmode) ? "" : workmode;
    }

    public void setWorkmode(String workmode) {
        this.workmode = workmode;
    }
}
