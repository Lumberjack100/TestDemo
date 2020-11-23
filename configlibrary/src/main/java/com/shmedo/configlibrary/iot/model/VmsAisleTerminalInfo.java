package com.shmedo.configlibrary.iot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：    Vms网关不同通道下，挂载终端的运行情况
 */
public class VmsAisleTerminalInfo {

    /**
     * mode : 1
     * netid : 2
     * chl : 20
     * addr : 2
     * terminalnum : 1
     * airbaud : 3
     * terminal : [{"sn":"253333D","addr":64,"uprssi":-74,"downrssi":-72,"tx":160,"rx":36,"volt":4.3,"status":1,"sensor_errno":[{"sensoraddr":0,"sensortype":58,"errno":-4,"sensorval":0},{"sensoraddr":1,"sensortype":58,"errno":-4,"sensorval":-0.448},{"sensoraddr":2,"sensortype":58,"errno":0,"sensorval":0.06},{"sensoraddr":3,"sensortype":58,"errno":0,"sensorval":0}],"logintime":"2020/09/28 11:23:24","lastpackagetime":"2020/09/28 11:23:24"}]
     */
    private int channel;

    private int mode;//通道工作模式 0：配置，1：数据
    private int netid;//网络号
    private int chl;//信道
    private int addr;//地址
    private int terminalnum;//终端接入数量
    private int airbaud;//空中速率
    private List<TerminalInfo> terminal;//终端列表

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getNetid() {
        return netid;
    }

    public void setNetid(int netid) {
        this.netid = netid;
    }

    public int getChl() {
        return chl;
    }

    public void setChl(int chl) {
        this.chl = chl;
    }

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
    }

    public int getTerminalnum() {
        return terminalnum;
    }

    public void setTerminalnum(int terminalnum) {
        this.terminalnum = terminalnum;
    }

    public List<TerminalInfo> getTerminal() {
        return terminal == null ? new ArrayList<TerminalInfo>() : terminal;
    }

    public void setTerminal(List<TerminalInfo> terminal) {
        this.terminal = terminal;
    }

    public int getAirbaud() {
        return airbaud;
    }

    public void setAirbaud(int airbaud) {
        this.airbaud = airbaud;
    }
}
