package com.shmedo.configlibrary.iot.cmd.entity.adme;

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

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (datatype != null && !datatype.equals("NullKey")) {
            stringBuilder.append("datatype=" + datatype);
            stringBuilder.append("&");
        }
        if (datareply != null && !datareply.equals("NullKey")) {
            stringBuilder.append("datareply=" + datareply);
            stringBuilder.append("&");
        }
        if (roundwaitetime != null && !roundwaitetime.equals("NullKey")) {
            stringBuilder.append("roundwaitetime=" + roundwaitetime);
            stringBuilder.append("&");
        }
        if (datainval != null && !datainval.equals("NullKey")) {
            stringBuilder.append("datainval=" + datainval);
            stringBuilder.append("&");
        }
        if (compensatetime != null && !compensatetime.equals("NullKey")) {
            stringBuilder.append("compensatetime=" + compensatetime);
            stringBuilder.append("&");
        }
        if (driveaddress != null && !driveaddress.equals("NullKey")) {
            stringBuilder.append("driveaddress=" + driveaddress);
            stringBuilder.append("&");
        }
        if (downspeed != null && !downspeed.equals("NullKey")) {
            stringBuilder.append("downspeed=" + downspeed);
            stringBuilder.append("&");
        }
        if (interdeep != null && !interdeep.equals("NullKey")) {
            stringBuilder.append("interdeep=" + interdeep);
            stringBuilder.append("&");
        }
        if (downwaitetime != null && !downwaitetime.equals("NullKey")) {
            stringBuilder.append("downwaitetime=" + downwaitetime);
            stringBuilder.append("&");
        }
        if (upspeed != null && !upspeed.equals("NullKey")) {
            stringBuilder.append("upspeed=" + upspeed);
            stringBuilder.append("&");
        }
        if (measpacing != null && !measpacing.equals("NullKey")) {
            stringBuilder.append("measpacing=" + measpacing);
            stringBuilder.append("&");
        }
        if (meaintertime != null && !meaintertime.equals("NullKey")) {
            stringBuilder.append("meaintertime=" + meaintertime);
            stringBuilder.append("&");
        }
        if (meabaseth != null && !meabaseth.equals("NullKey")) {
            stringBuilder.append("meabaseth=" + meabaseth);
            stringBuilder.append("&");
        }
        if (dwonblocked != null && !dwonblocked.equals("NullKey")) {
            stringBuilder.append("dwonblocked=" + dwonblocked);
            stringBuilder.append("&");
        }
        if (untimenum != null && !untimenum.equals("NullKey")) {
            stringBuilder.append("untimenum=" + untimenum);
            stringBuilder.append("&");
        }
        if (detectiontime != null && !detectiontime.equals("NullKey")) {
            stringBuilder.append("detectiontime=" + detectiontime);
            stringBuilder.append("&");
        }
        if (detectionstart != null && !detectionstart.equals("NullKey")) {
            stringBuilder.append("detectionstart=" + detectionstart);
            stringBuilder.append("&");
        }
        if (detectionend != null && !detectionend.equals("NullKey")) {
            stringBuilder.append("detectionend=" + detectionend);
            stringBuilder.append("&");
        }
        if (interval_compensation != null && !interval_compensation.equals("NullKey")) {
            stringBuilder.append("interval_compensation=" + interval_compensation);
            stringBuilder.append("&");
        }
        if (interval_fitting != null && !interval_fitting.equals("NullKey")) {
            stringBuilder.append("interval_fitting=" + interval_fitting);
            stringBuilder.append("&");
        }
        if (point_offset != null && !point_offset.equals("NullKey")) {
            stringBuilder.append("point_offset=" + point_offset);
            stringBuilder.append("&");
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
