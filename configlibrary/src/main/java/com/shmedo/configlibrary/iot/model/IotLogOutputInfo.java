package com.shmedo.configlibrary.iot.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/11 <br/>
 * 描述：     日志输出方式
 */
public class IotLogOutputInfo {
    private String level;
    private String type;

    public String getLevel() {
        return TextUtils.isEmpty(level) ? "off" : level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
