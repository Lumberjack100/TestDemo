package com.shmedo.configlibrary.iot.cmd.entity;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/23/20 <br/>
 * 描述：     设置Vms终端某个通道下传感器参数
 */
public class SetTerminalSensorParamsEntity implements Validater {
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

    public void setSn(String sn) {
        this.sn = sn;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setInsert(String insert) {
        this.insert = insert;
    }

    public void setFreqtype(String freqtype) {
        this.freqtype = freqtype;
    }

    public void setFreqmax(String freqmax) {
        this.freqmax = freqmax;
    }

    public void setFreqmin(String freqmin) {
        this.freqmin = freqmin;
    }

    public void setVolttype(String volttype) {
        this.volttype = volttype;
    }

    public void setExpvolt(String expvolt) {
        this.expvolt = expvolt;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGateval(String gateval) {
        this.gateval = gateval;
    }

    public void setCorral(String corral) {
        this.corral = corral;
    }

    public void setFixsite(String fixsite) {
        this.fixsite = fixsite;
    }

    public void setRopelen(String ropelen) {
        this.ropelen = ropelen;
    }

    public void setParama(String parama) {
        this.parama = parama;
    }

    public void setParamb(String paramb) {
        this.paramb = paramb;
    }

    public void setParamc(String paramc) {
        this.paramc = paramc;
    }

    public void setParamk(String paramk) {
        this.paramk = paramk;
    }

    public void setParamm(String paramm) {
        this.paramm = paramm;
    }

    public void setParamf(String paramf) {
        this.paramf = paramf;
    }

    public void setParamt(String paramt) {
        this.paramt = paramt;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sn=" + sn);
        stringBuilder.append("&");

        stringBuilder.append("channel=" + channel);
        stringBuilder.append("&");

        if (!TextUtils.isEmpty(insert)) {
            stringBuilder.append("insert=" + insert);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(freqtype)) {
            stringBuilder.append("freqtype=" + freqtype);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(freqmax)) {
            stringBuilder.append("freqmax=" + freqmax);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(freqmin)) {
            stringBuilder.append("freqmin=" + freqmin);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(volttype)) {
            stringBuilder.append("volttype=" + volttype);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(expvolt)) {
            stringBuilder.append("expvolt=" + expvolt);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(type)) {
            stringBuilder.append("type=" + type);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(name)) {
            stringBuilder.append("name=" + name);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(gateval)) {
            stringBuilder.append("gateval=" + gateval);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(corral)) {
            stringBuilder.append("corral=" + corral);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(fixsite)) {
            stringBuilder.append("fixsite=" + fixsite);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(ropelen)) {
            stringBuilder.append("ropelen=" + ropelen);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(parama)) {
            stringBuilder.append("parama=" + parama);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(paramb)) {
            stringBuilder.append("paramb=" + paramb);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(paramc)) {
            stringBuilder.append("paramc=" + paramc);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(paramk)) {
            stringBuilder.append("paramk=" + paramk);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(paramm)) {
            stringBuilder.append("paramm=" + paramm);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(paramf)) {
            stringBuilder.append("paramf=" + paramf);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(paramt)) {
            stringBuilder.append("paramt=" + paramt);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
