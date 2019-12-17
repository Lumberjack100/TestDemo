package com.shmedo.mcloudapp.entity.parameter;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 创建者:   dpc
 * 创建时间:  2019-12-16
 * 描述：   查询设备参数实体类
 */
public class QueryCloudDataParameter {
    private String sn;
    private String begin;
    private String end;
    private String number;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
