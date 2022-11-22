package com.shmedo.configlibrary.iot.model.hac;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/29/20 <br/>
 * 描述：     ADME 执行机构参数
 */
public class HacExecutiveAgencyInfo {
    private String datatype;//数据解算方式（0:顶部固定法，1底部固定法）
    private String datareply;//数据应答（0:关闭，1:启用）
    private String datainval;//数据读取间隔
    private String compensatetime;//测量补偿时间
    private String driveaddress;//电机驱动器地址
    private String downspeed;//电机下放速度
    private String downwaitetime;//下放等待时间
    private String upspeed;//电机上拉速度
    private String measpacing;//测量间距
    private String meaintertime;//测量间隔时间
    private String interval_compensation;//距离补偿区间h1
    private String interval_fitting;//数据拟合区间h2
    private String point_offset;//测点偏移距离h3


    public String getDatatype() {
        return TextUtils.isEmpty(datatype) ? "" : datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getDatareply() {
        return TextUtils.isEmpty(datareply) ? "" : datareply;
    }

    public void setDatareply(String datareply) {
        this.datareply = datareply;
    }

    public String getDatainval() {
        return TextUtils.isEmpty(datainval) ? "" : datainval;
    }

    public void setDatainval(String datainval) {
        this.datainval = datainval;
    }

    public String getCompensatetime() {
        return TextUtils.isEmpty(compensatetime) ? "" : compensatetime;
    }

    public void setCompensatetime(String compensatetime) {
        this.compensatetime = compensatetime;
    }

    public String getDriveaddress() {
        return TextUtils.isEmpty(driveaddress) ? "" : driveaddress;
    }

    public void setDriveaddress(String driveaddress) {
        this.driveaddress = driveaddress;
    }

    public String getDownspeed() {
        return TextUtils.isEmpty(downspeed) ? "" : downspeed;
    }

    public void setDownspeed(String downspeed) {
        this.downspeed = downspeed;
    }


    public String getDownwaitetime() {
        return TextUtils.isEmpty(downwaitetime) ? "" : downwaitetime;
    }

    public void setDownwaitetime(String downwaitetime) {
        this.downwaitetime = downwaitetime;
    }

    public String getUpspeed() {
        return TextUtils.isEmpty(upspeed) ? "" : upspeed;
    }

    public void setUpspeed(String upspeed) {
        this.upspeed = upspeed;
    }

    public String getMeaspacing() {
        return TextUtils.isEmpty(measpacing) ? "" : measpacing;
    }

    public void setMeaspacing(String measpacing) {
        this.measpacing = measpacing;
    }

    public String getMeaintertime() {
        return TextUtils.isEmpty(meaintertime) ? "" : meaintertime;
    }

    public void setMeaintertime(String meaintertime) {
        this.meaintertime = meaintertime;
    }


    public String getInterval_compensation() {
        return TextUtils.isEmpty(interval_compensation) ? "" : interval_compensation;
    }

    public void setInterval_compensation(String interval_compensation) {
        this.interval_compensation = interval_compensation;
    }

    public String getInterval_fitting() {
        return TextUtils.isEmpty(interval_fitting) ? "" : interval_fitting;
    }

    public void setInterval_fitting(String interval_fitting) {
        this.interval_fitting = interval_fitting;
    }

    public String getPoint_offset() {
        return TextUtils.isEmpty(point_offset) ? "" : point_offset;
    }

    public void setPoint_offset(String point_offset) {
        this.point_offset = point_offset;
    }

}
