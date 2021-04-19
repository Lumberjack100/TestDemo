package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/19 <br/>
 * 描述：     开关量传感器参数
 */
public class DasIOSensorInfo {
    private String type;//0：关闭开关量功能 1：雨量站模式 2：断线报警器模式
    private String value;//当type取1时，value代表雨量计精度  当type取2时，value代表断线报警器状态，0：常开，1：常关

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return TextUtils.isEmpty(value) ? "" : value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
