package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/29/20 <br/>
 * 描述：     ADME 执行机构参数
 */
public class AdmeExecutiveAgencyInfo {
    private String meastype;//测量方式（0:实时测量，1:整时整点测量，2:定时定点测量）
    private String datatype;//数据解算方式（0:顶固定法，1底固定法）
    private String datareply;//数据应答（0:关闭，1:启用）
    private String roundwaitetime;//每轮等待时间
    private String roundmeasinval;//每轮测量间隔
    private String roundmeasstart;//每轮测量开始时间
    private String datainval;//数据读取间隔
    private String compensatetime;//测量补偿时间
    private String driveaddress;//电机驱动器地址
    private String downspeed;//电机下放速度
    private String interdeep;//测斜管孔深
    private String downwaitetime;//下放等待时间
    private String upspeed;//电机上拉速度
    private String measpacing;//测量间距
    private String meaintertime;//测量间隔时间
    private String meabaseth;//测量基准深度
    private String dwonblocked;//下放堵转预判（0:关闭，1:开启）
    private String untimenum;//堵转单位时间脉冲数
    private String detectiontime;//堵转检测判断时间
    private String detectionstart;//堵转检测起点
    private String detectionend;//堵转检测终点
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

    public String getRoundwaitetime() {
        return TextUtils.isEmpty(roundwaitetime) ? "" : roundwaitetime;
    }

    public void setRoundwaitetime(String roundwaitetime) {
        this.roundwaitetime = roundwaitetime;
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

    public String getInterdeep() {
        return TextUtils.isEmpty(interdeep) ? "0" : interdeep;
    }

    public void setInterdeep(String interdeep) {
        this.interdeep = interdeep;
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

    public String getMeabaseth() {
        return TextUtils.isEmpty(meabaseth) ? "0" : meabaseth;
    }

    public void setMeabaseth(String meabaseth) {
        this.meabaseth = meabaseth;
    }

    public String getDwonblocked() {
        return TextUtils.isEmpty(dwonblocked) ? "" : dwonblocked;
    }

    public void setDwonblocked(String dwonblocked) {
        this.dwonblocked = dwonblocked;
    }

    public String getUntimenum() {
        return TextUtils.isEmpty(untimenum) ? "" : untimenum;
    }

    public void setUntimenum(String untimenum) {
        this.untimenum = untimenum;
    }

    public String getDetectiontime() {
        return TextUtils.isEmpty(detectiontime) ? "0" : detectiontime;
    }

    public void setDetectiontime(String detectiontime) {
        this.detectiontime = detectiontime;
    }

    public String getDetectionstart() {
        return TextUtils.isEmpty(detectionstart) ? "" : detectionstart;
    }

    public void setDetectionstart(String detectionstart) {
        this.detectionstart = detectionstart;
    }

    public String getDetectionend() {
        return TextUtils.isEmpty(detectionend) ? "" : detectionend;
    }

    public void setDetectionend(String detectionend) {
        this.detectionend = detectionend;
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

    public String getMeastype() {
        return TextUtils.isEmpty(meastype) ? "0" : meastype;
    }

    public void setMeastype(String meastype) {
        this.meastype = meastype;
    }

    public String getRoundmeasinval() {
        return TextUtils.isEmpty(roundmeasinval) ? "" : roundmeasinval;
    }

    public void setRoundmeasinval(String roundmeasinval) {
        this.roundmeasinval = roundmeasinval;
    }

    public String getRoundmeasstart() {
        return TextUtils.isEmpty(roundmeasstart) ? "" : roundmeasstart;
    }

    public void setRoundmeasstart(String roundmeasstart) {
        this.roundmeasstart = roundmeasstart;
    }
}
