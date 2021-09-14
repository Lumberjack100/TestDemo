package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/15 <br/>
 * 描述：     DAS 状态页面主传感器状态
 */
public class DasSensorStatusInfo {
    private String type;//传感器类型
    private int addr;//传感器地址
    private int errno;//错误码
    private String val;//传感器数据


    public String getType() {
        return TextUtils.isEmpty(type) ? "0" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

}
