package com.shmedo.lib.device.base.iot_cmd.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/16 <br/>
 * 描述：   DAS 状态页面数字水位计状态
 */
public class VwpBean {
    private int type;
    private String vaule;
    private int errno;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return TextUtils.isEmpty(vaule) ? "0" : vaule;
    }

    public void setValue(String value) {
        this.vaule = value;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }
}
