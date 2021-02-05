package com.shmedo.configlibrary.iot.cmd.entity.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/29/20 <br/>
 * 描述：     生成ADME 执行机构配置参数拼接指令
 */
public class AdmeExecutiveAgencyEntity implements Validater {
    private String datatype;//数据结算方式（0:顶固定法，1底固定法）
    private String datareply;//数据应答（0:关闭，1:启用）
    private String roundwaitetime;//每轮等待时间
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

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("datatype=" + datatype);
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(datareply)) {
            stringBuilder.append("datareply=" + datareply);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(roundwaitetime)) {
            stringBuilder.append("roundwaitetime=" + roundwaitetime);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(datainval)) {
            stringBuilder.append("datainval=" + datainval);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(compensatetime)) {
            stringBuilder.append("compensatetime=" + compensatetime);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(driveaddress)) {
            stringBuilder.append("driveaddress=" + driveaddress);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(downspeed)) {
            stringBuilder.append("downspeed=" + downspeed);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(interdeep)) {
            stringBuilder.append("interdeep=" + interdeep);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(downwaitetime)) {
            stringBuilder.append("downwaitetime=" + downwaitetime);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(upspeed)) {
            stringBuilder.append("upspeed=" + upspeed);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(measpacing)) {
            stringBuilder.append("measpacing=" + measpacing);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(meaintertime)) {
            stringBuilder.append("meaintertime=" + meaintertime);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(meabaseth)) {
            stringBuilder.append("meabaseth=" + meabaseth);
            stringBuilder.append("&");
        }

        if (!TextUtils.isEmpty(dwonblocked)) {
            stringBuilder.append("dwonblocked=" + dwonblocked);
            stringBuilder.append("&");
        }

        if (!TextUtils.isEmpty(untimenum)) {
            stringBuilder.append("untimenum=" + untimenum);
            stringBuilder.append("&");
        }

        if (!TextUtils.isEmpty(detectiontime)) {
            stringBuilder.append("detectiontime=" + detectiontime);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(detectionstart)) {
            stringBuilder.append("detectionstart=" + detectionstart);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(detectionend)) {
            stringBuilder.append("detectionend=" + detectionend);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
