package com.shmedo.lib.device.base.iot_cmd.model.vms;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/23/20 <br/>
 * 描述：    终端接入的传感器信息
 */
public class VmsTerminalSensorInfo implements Parcelable {
    private String sn;
    private String channel;//传感器所在通道
    private String insert;//接入判断，0：未接入，1：接入
    private String freqtype;//激励类型，默认4（频率反馈固定频率扫频法）
    private String freqmax;//频率上限，默认2000
    private String freqmin;//频率下限，默认1000
    private String volttype;//激励电压类型，0：低压，1：高压，默认0
    private String expvolt;//期望电压,高压激励时的期望电压，默认150
    private String type;//传感器类型，默认55，振弦式裂缝计
    private String name;//传感器名称，默认102_x，x为通道号
    private String gateval;//触发阈值，默认10
    private String corral;//修正值，默认为0
    private String fixsite;//安装高程,默认为0，单位m
    private String ropelen;//绳长，默认为0，单位m
    private String parama;//修正系数A
    private String paramb;//修正系数B
    private String paramc;//修正系数C
    private String paramk;//修正系数K
    private String paramm;//修正系数M
    private String paramf;//基准值
    private String paramt;//初始温度

    public VmsTerminalSensorInfo() {
    }


    protected VmsTerminalSensorInfo(Parcel in) {
        sn = in.readString();
        channel = in.readString();
        insert = in.readString();
        freqtype = in.readString();
        freqmax = in.readString();
        freqmin = in.readString();
        volttype = in.readString();
        expvolt = in.readString();
        type = in.readString();
        name = in.readString();
        gateval = in.readString();
        corral = in.readString();
        fixsite = in.readString();
        ropelen = in.readString();
        parama = in.readString();
        paramb = in.readString();
        paramc = in.readString();
        paramk = in.readString();
        paramm = in.readString();
        paramf = in.readString();
        paramt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sn);
        dest.writeString(channel);
        dest.writeString(insert);
        dest.writeString(freqtype);
        dest.writeString(freqmax);
        dest.writeString(freqmin);
        dest.writeString(volttype);
        dest.writeString(expvolt);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeString(gateval);
        dest.writeString(corral);
        dest.writeString(fixsite);
        dest.writeString(ropelen);
        dest.writeString(parama);
        dest.writeString(paramb);
        dest.writeString(paramc);
        dest.writeString(paramk);
        dest.writeString(paramm);
        dest.writeString(paramf);
        dest.writeString(paramt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VmsTerminalSensorInfo> CREATOR = new Creator<VmsTerminalSensorInfo>() {
        @Override
        public VmsTerminalSensorInfo createFromParcel(Parcel in) {
            return new VmsTerminalSensorInfo(in);
        }

        @Override
        public VmsTerminalSensorInfo[] newArray(int size) {
            return new VmsTerminalSensorInfo[size];
        }
    };

    public String getSn() {
        return TextUtils.isEmpty(sn) ? "" : sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getChannel() {
        return TextUtils.isEmpty(channel) ? "" : channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getInsert() {
        return TextUtils.isEmpty(insert) ? "" : insert;
    }

    public void setInsert(String insert) {
        this.insert = insert;
    }

    public String getFreqtype() {
        return TextUtils.isEmpty(freqtype) ? "" : freqtype;
    }

    public void setFreqtype(String freqtype) {
        this.freqtype = freqtype;
    }

    public String getFreqmax() {
        return TextUtils.isEmpty(freqmax) ? "" : freqmax;
    }

    public void setFreqmax(String freqmax) {
        this.freqmax = freqmax;
    }

    public String getFreqmin() {
        return TextUtils.isEmpty(freqmin) ? "" : freqmin;
    }

    public void setFreqmin(String freqmin) {
        this.freqmin = freqmin;
    }

    public String getVolttype() {
        return TextUtils.isEmpty(volttype) ? "" : volttype;
    }

    public void setVolttype(String volttype) {
        this.volttype = volttype;
    }

    public String getExpvolt() {
        return TextUtils.isEmpty(expvolt) ? "" : expvolt;
    }

    public void setExpvolt(String expvolt) {
        this.expvolt = expvolt;
    }

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGateval() {
        return TextUtils.isEmpty(gateval) ? "" : gateval;
    }

    public void setGateval(String gateval) {
        this.gateval = gateval;
    }

    public String getCorral() {
        return TextUtils.isEmpty(corral) ? "" : corral;
    }

    public void setCorral(String corral) {
        this.corral = corral;
    }

    public String getFixsite() {
        return TextUtils.isEmpty(fixsite) ? "" : fixsite;
    }

    public void setFixsite(String fixsite) {
        this.fixsite = fixsite;
    }

    public String getRopelen() {
        return TextUtils.isEmpty(ropelen) ? "" : ropelen;
    }

    public void setRopelen(String ropelen) {
        this.ropelen = ropelen;
    }

    public String getParama() {
        return TextUtils.isEmpty(parama) ? "" : parama;
    }

    public void setParama(String parama) {
        this.parama = parama;
    }

    public String getParamb() {
        return TextUtils.isEmpty(paramb) ? "" : paramb;
    }

    public void setParamb(String paramb) {
        this.paramb = paramb;
    }

    public String getParamc() {
        return TextUtils.isEmpty(paramc) ? "" : paramc;
    }

    public void setParamc(String paramc) {
        this.paramc = paramc;
    }

    public String getParamk() {
        return TextUtils.isEmpty(paramk) ? "" : paramk;
    }

    public void setParamk(String paramk) {
        this.paramk = paramk;
    }

    public String getParamm() {
        return TextUtils.isEmpty(paramm) ? "" : paramm;
    }

    public void setParamm(String paramm) {
        this.paramm = paramm;
    }

    public String getParamf() {
        return TextUtils.isEmpty(paramf) ? "" : paramf;
    }

    public void setParamf(String paramf) {
        this.paramf = paramf;
    }

    public String getParamt() {
        return TextUtils.isEmpty(paramt) ? "" : paramt;
    }

    public void setParamt(String paramt) {
        this.paramt = paramt;
    }
}
