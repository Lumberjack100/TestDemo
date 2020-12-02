package com.shmedo.configlibrary.iot.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/29/20 <br/>
 * 描述：     数据中心状态
 */
public class VmsDataCenterStatus {
    private int centerid;//数据中心编号
    private String status;//0未开启，1已上线，2未上线

    public int getCenterid() {
        return centerid;
    }

    public void setCenterid(int centerid) {
        this.centerid = centerid;
    }

    public String getStatus() {
        return TextUtils.isEmpty(status) ? "" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
