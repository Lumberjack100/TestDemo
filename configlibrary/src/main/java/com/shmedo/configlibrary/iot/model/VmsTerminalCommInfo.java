package com.shmedo.configlibrary.iot.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/2/20 <br/>
 * 描述：    Vms网关挂载的终端通信参数
 */
public class VmsTerminalCommInfo {

    private String netid;//网络号
    private String dstaddr;//通道的地址
    private String channel;//通信信道
    private String airbaud;//空中波特率

    public String getNetid() {
        return netid;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }

    public String getDstaddr() {
        return dstaddr;
    }

    public void setDstaddr(String dstaddr) {
        this.dstaddr = dstaddr;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAirbaud() {
        return airbaud;
    }

    public void setAirbaud(String airbaud) {
        this.airbaud = airbaud;
    }
}
