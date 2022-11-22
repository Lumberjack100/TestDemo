package com.shmedo.configlibrary.iot.cmd.entity.hac;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/29 <br/>
 * 描述：     TODO
 */
public class HacExecutiveAgencyInfoEntity implements Validater {
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

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public void setDatareply(String datareply) {
        this.datareply = datareply;
    }

    public void setDatainval(String datainval) {
        this.datainval = datainval;
    }

    public void setCompensatetime(String compensatetime) {
        this.compensatetime = compensatetime;
    }

    public void setDriveaddress(String driveaddress) {
        this.driveaddress = driveaddress;
    }

    public void setDownspeed(String downspeed) {
        this.downspeed = downspeed;
    }

    public void setDownwaitetime(String downwaitetime) {
        this.downwaitetime = downwaitetime;
    }

    public void setUpspeed(String upspeed) {
        this.upspeed = upspeed;
    }

    public void setMeaspacing(String measpacing) {
        this.measpacing = measpacing;
    }

    public void setMeaintertime(String meaintertime) {
        this.meaintertime = meaintertime;
    }

    public void setInterval_compensation(String interval_compensation) {
        this.interval_compensation = interval_compensation;
    }

    public void setInterval_fitting(String interval_fitting) {
        this.interval_fitting = interval_fitting;
    }

    public void setPoint_offset(String point_offset) {
        this.point_offset = point_offset;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (Field f : getClass().getDeclaredFields()) {
                Object value = f.get(this);
                if (value != null && !value.equals("NullKey")) {
                    stringBuilder.append(f.getName());
                    stringBuilder.append("=");
                    stringBuilder.append(value);
                    stringBuilder.append("&");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
