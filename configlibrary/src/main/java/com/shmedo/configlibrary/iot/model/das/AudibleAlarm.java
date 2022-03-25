package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：     声光报警器信息
 */
public class AudibleAlarm {
    private String channel;//通信信道
    private String panid;//网络编号
    private String groupid;//目标地址
    private String alarmtype;//报警类型

    public String getChannel() {
        return TextUtils.isEmpty(channel) ? "" : channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getPanid() {
        return TextUtils.isEmpty(panid) ? "" : panid;
    }

    public void setPanid(String panid) {
        this.panid = panid;
    }

    public String getGroupid() {
        return TextUtils.isEmpty(groupid) ? "" : groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getAlarmtype() {
        return TextUtils.isEmpty(alarmtype) ? "" : alarmtype;
    }

    public void setAlarmtype(String alarmtype) {
        this.alarmtype = alarmtype;
    }
}
