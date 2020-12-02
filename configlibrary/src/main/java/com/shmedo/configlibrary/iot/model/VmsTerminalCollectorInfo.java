package com.shmedo.configlibrary.iot.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/2/20 <br/>
 * 描述：    Vms网关挂载的终端采集参数
 */
public class VmsTerminalCollectorInfo {

    private String reptgap;//数据上报间隔，单位s
    private String repttype;//数据上报方式，0：网关召测，1：主动上报
    private String filtertype;//滤波类型，默认0（无滤波）1，中值滤波；2，算术平均滤波；3，中位值平均滤波；4，加权平均滤波
    private String filternum;//样本大小，默认10
    private String collgap;//采集间隔，默认10
    private String waitgap;//激励前等待间隔，默认500，单位ms

    public String getReptgap() {
        return reptgap;
    }

    public void setReptgap(String reptgap) {
        this.reptgap = reptgap;
    }

    public String getRepttype() {
        return repttype;
    }

    public void setRepttype(String repttype) {
        this.repttype = repttype;
    }

    public String getFiltertype() {
        return filtertype;
    }

    public void setFiltertype(String filtertype) {
        this.filtertype = filtertype;
    }

    public String getFilternum() {
        return filternum;
    }

    public void setFilternum(String filternum) {
        this.filternum = filternum;
    }

    public String getCollgap() {
        return collgap;
    }

    public void setCollgap(String collgap) {
        this.collgap = collgap;
    }

    public String getWaitgap() {
        return waitgap;
    }

    public void setWaitgap(String waitgap) {
        this.waitgap = waitgap;
    }
}
