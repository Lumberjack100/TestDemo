package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/20 <br/>
 * 描述：     数据上报时间
 */
public class DasDataReportInfo {
    private String report_intv;//数据上报间隔
    private String plus_intv;//加报间隔
    private String plus_count;//加报次数

    public String getReport_intv() {
        return TextUtils.isEmpty(report_intv) ? "" : report_intv;
    }

    public void setReport_intv(String report_intv) {
        this.report_intv = report_intv;
    }

    public String getPlus_intv() {
        return TextUtils.isEmpty(plus_intv) ? "" : plus_intv;
    }

    public void setPlus_intv(String plus_intv) {
        this.plus_intv = plus_intv;
    }

    public String getPlus_count() {
        return TextUtils.isEmpty(plus_count) ? "" : plus_count;
    }

    public void setPlus_count(String plus_count) {
        this.plus_count = plus_count;
    }
}
