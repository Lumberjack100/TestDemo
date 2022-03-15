package com.shmedo.configlibrary.iot.cmd.entity.adme;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/29/20 <br/>
 * 描述：     生成ADME 执行机构配置参数拼接指令
 */
public class AdmeExecutiveAgencyEntity implements Validater {
    private String meastype;//测量方式（0:实时测量，1:整时整点测量，2:定时定点测量）
    private String datatype;//数据结算方式（0:顶固定法，1底固定法）
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

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public void setDatareply(String datareply) {
        this.datareply = datareply;
    }

    public void setRoundwaitetime(String roundwaitetime) {
        this.roundwaitetime = roundwaitetime;
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

    public void setInterdeep(String interdeep) {
        this.interdeep = interdeep;
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

    public void setMeabaseth(String meabaseth) {
        this.meabaseth = meabaseth;
    }

    public void setDwonblocked(String dwonblocked) {
        this.dwonblocked = dwonblocked;
    }

    public void setUntimenum(String untimenum) {
        this.untimenum = untimenum;
    }

    public void setDetectiontime(String detectiontime) {
        this.detectiontime = detectiontime;
    }

    public void setDetectionstart(String detectionstart) {
        this.detectionstart = detectionstart;
    }

    public void setDetectionend(String detectionend) {
        this.detectionend = detectionend;
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

    public void setMeastype(String meastype) {
        this.meastype = meastype;
    }

    public void setRoundmeasinval(String roundmeasinval) {
        this.roundmeasinval = roundmeasinval;
    }

    public void setRoundmeasstart(String roundmeasstart) {
        this.roundmeasstart = roundmeasstart;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(assembleCharacters("meastype", meastype));
//        stringBuilder.append(assembleCharacters("datatype", datatype));
//        stringBuilder.append(assembleCharacters("datareply", datareply));
//        stringBuilder.append(assembleCharacters("roundwaitetime", roundwaitetime));
//        stringBuilder.append(assembleCharacters("roundmeasinval", roundmeasinval));
//        stringBuilder.append(assembleCharacters("datainval", datainval));
//        stringBuilder.append(assembleCharacters("compensatetime", compensatetime));
//        stringBuilder.append(assembleCharacters("driveaddress", driveaddress));
//        stringBuilder.append(assembleCharacters("interdeep", interdeep));
//        stringBuilder.append(assembleCharacters("downwaitetime", downwaitetime));
//        stringBuilder.append(assembleCharacters("upspeed", upspeed));
//        stringBuilder.append(assembleCharacters("measpacing", measpacing));
//        stringBuilder.append(assembleCharacters("meaintertime", meaintertime));
//        stringBuilder.append(assembleCharacters("meabaseth", meabaseth));
//        stringBuilder.append(assembleCharacters("dwonblocked", dwonblocked));
//        stringBuilder.append(assembleCharacters("untimenum", untimenum));
//        stringBuilder.append(assembleCharacters("detectiontime", detectiontime));

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
        Timber.d("assembleCmdCharacters1 :%s", stringBuilder.toString());
        return stringBuilder.toString();
    }

    private String assembleCharacters(String fieldName, String fieldValue) {
        String res = "";
        if (fieldValue != null && !fieldValue.equals("NullKey")) {
            res = String.format("%s=%s&", fieldName, fieldValue);
        }
        return res;
    }
}
