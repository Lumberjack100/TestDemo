package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：     ADME的运行状态
 */
public class AdmeMotionState {
    private String motionstate;//运动状态(0:管口停止，1:管底停止，2:管口测量，3:管口测试，4:上拉测量，5:上拉测试，6:下放测量，7:下放测试)
    private String inctiondis;//测斜仪运动距离或实时下放距离或管底等待时的距离
    private String measmode;//测量模式 (0：`正测  1：反测)
    //电机运动信息(0：磁开关触发 1：测斜仪配对,设置参数 2：测斜仪下放 3：管底等待 4：测点测量 5：磁开关触发，测量结束 6：测斜仪配对,读取数据  7：数据上传  8：周期等待)
    //注：单次测量过程 0、1、2、3、4、5、6、7、8
    //   正反测量过程 0、1、2、3、4、5、6、8—》0、1、2、3、4、5、6、8
    private String motorinfo;//电机运动信息
    private String measpoint;//测点信息 (1|20.5  表示第一个测量点：20.5米)
    private String waittime;//管底等待时间

    public String getMotionstate() {
        return TextUtils.isEmpty(motionstate) ? "" : motionstate;
    }

    public void setMotionstate(String motionstate) {
        this.motionstate = motionstate;
    }

    public String getInctiondis() {
        return TextUtils.isEmpty(inctiondis) ? "" : inctiondis;
    }

    public void setInctiondis(String inctiondis) {
        this.inctiondis = inctiondis;
    }

    public String getMeasmode() {
        return TextUtils.isEmpty(measmode) ? "" : measmode;
    }

    public void setMeasmode(String measmode) {
        this.measmode = measmode;
    }

    public String getMotorinfo() {
        return TextUtils.isEmpty(motorinfo) ? "" : motorinfo;
    }

    public void setMotorinfo(String motorinfo) {
        this.motorinfo = motorinfo;
    }

    public String getMeaspoint() {
        return TextUtils.isEmpty(measpoint) ? "" : measpoint;
    }

    public void setMeaspoint(String measpoint) {
        this.measpoint = measpoint;
    }

    public String getWaittime() {
        return TextUtils.isEmpty(waittime) ? "" : waittime;
    }

    public void setWaittime(String waittime) {
        this.waittime = waittime;
    }
}
