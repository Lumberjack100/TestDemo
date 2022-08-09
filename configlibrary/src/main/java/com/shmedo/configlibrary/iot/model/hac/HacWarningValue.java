package com.shmedo.configlibrary.iot.model.hac;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/20 <br/>
 * 描述：     预警值
 */
public class HacWarningValue {
    private String x1min;//一级预警 X 轴最小值
    private String x1max;//一级预警 X 轴最大值
    private String y1min;//一级预警 Y 轴最小值
    private String y1max;//一级预警 Y 轴最大值
    private String x2min;//二级预警 X 轴最小值
    private String x2max;//二级预警 X 轴最大值
    private String y2min;//二级预警 Y 轴最小值
    private String y2max;//二级预警 Y 轴最大值
    private String x3min;//三级预警 X 轴最小值
    private String x3max;//三级预警 X 轴最大值
    private String y3min;//三级预警 Y 轴最小值
    private String y3max;//三级预警 Y 轴最大值

    public String getX1min() {
        return TextUtils.isEmpty(x1min) ? "0" : x1min;
    }

    public void setX1min(String x1min) {
        this.x1min = x1min;
    }

    public String getX1max() {
        return TextUtils.isEmpty(x1max) ? "0" : x1max;
    }

    public void setX1max(String x1max) {
        this.x1max = x1max;
    }

    public String getY1min() {
        return TextUtils.isEmpty(y1min) ? "0" : y1min;
    }

    public void setY1min(String y1min) {
        this.y1min = y1min;
    }

    public String getY1max() {
        return TextUtils.isEmpty(y1max) ? "0" : y1max;
    }

    public void setY1max(String y1max) {
        this.y1max = y1max;
    }

    public String getX2min() {
        return TextUtils.isEmpty(x2min) ? "0" : x2min;
    }

    public void setX2min(String x2min) {
        this.x2min = x2min;
    }

    public String getX2max() {
        return TextUtils.isEmpty(x2max) ? "0" : x2max;
    }

    public void setX2max(String x2max) {
        this.x2max = x2max;
    }

    public String getY2min() {
        return TextUtils.isEmpty(y2min) ? "0" : y2min;
    }

    public void setY2min(String y2min) {
        this.y2min = y2min;
    }

    public String getY2max() {
        return TextUtils.isEmpty(y2max) ? "0" : y2max;
    }

    public void setY2max(String y2max) {
        this.y2max = y2max;
    }

    public String getX3min() {
        return TextUtils.isEmpty(x3min) ? "0" : x3min;
    }

    public void setX3min(String x3min) {
        this.x3min = x3min;
    }

    public String getX3max() {
        return TextUtils.isEmpty(x3max) ? "0" : x3max;
    }

    public void setX3max(String x3max) {
        this.x3max = x3max;
    }

    public String getY3min() {
        return TextUtils.isEmpty(y3min) ? "0" : y3min;
    }

    public void setY3min(String y3min) {
        this.y3min = y3min;
    }

    public String getY3max() {
        return TextUtils.isEmpty(y3max) ? "0" : y3max;
    }

    public void setY3max(String y3max) {
        this.y3max = y3max;
    }
}
