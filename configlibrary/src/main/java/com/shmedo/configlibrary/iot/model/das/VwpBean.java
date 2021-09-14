package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/16 <br/>
 * 描述：   DAS 状态页面数字式渗压计状态
 */
public class VwpBean {
    private int type;
    private String value;
    private int errno;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return TextUtils.isEmpty(value) ? "0" : value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }
}
