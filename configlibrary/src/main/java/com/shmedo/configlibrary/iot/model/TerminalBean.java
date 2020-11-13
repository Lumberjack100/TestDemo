package com.shmedo.configlibrary.iot.model;

import android.text.TextUtils;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：    Vms网关挂载的终端设备信息
 */
public class TerminalBean {
    /**
     * sn : 253333D
     * addr : 64
     * uprssi : -74
     * downrssi : -72
     * tx : 160
     * rx : 36
     * volt : 4.3
     * status : 1
     * sensor_errno : [{"sensoraddr":0,"sensortype":58,"errno":-4,"sensorval":0},{"sensoraddr":1,"sensortype":58,"errno":-4,"sensorval":-0.448},{"sensoraddr":2,"sensortype":58,"errno":0,"sensorval":0.06},{"sensoraddr":3,"sensortype":58,"errno":0,"sensorval":0}]
     * logintime : 2020/09/28 11:23:24
     * lastpackagetime : 2020/09/28 11:23:24
     */

    private String sn;//终端SN号
    private int addr;//终端地址
    private int uprssi;//上行信号强度
    private int downrssi;//下行信号强度
    private int tx;//发送数据
    private int rx;//接收数据
    private Double volt;//终端电压
    private int status;//终端在线状态 0:离线，1：在线
    private String logintime;//注册时间
    private String lastpackagetime;//最后交互时间
    private List<SensorErrnoBean> sensor_errno;

    public String getSn() {
        return TextUtils.isEmpty(sn) ? "" : sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
    }

    public int getUprssi() {
        return uprssi;
    }

    public void setUprssi(int uprssi) {
        this.uprssi = uprssi;
    }

    public int getDownrssi() {
        return downrssi;
    }

    public void setDownrssi(int downrssi) {
        this.downrssi = downrssi;
    }

    public int getTx() {
        return tx;
    }

    public void setTx(int tx) {
        this.tx = tx;
    }

    public int getRx() {
        return rx;
    }

    public void setRx(int rx) {
        this.rx = rx;
    }

    public Double getVolt() {
        return volt;
    }

    public void setVolt(Double volt) {
        this.volt = volt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLogintime() {
        return TextUtils.isEmpty(logintime) ? "" : logintime;
    }

    public void setLogintime(String logintime) {
        this.logintime = logintime;
    }

    public String getLastpackagetime() {
        return TextUtils.isEmpty(lastpackagetime) ? "" : lastpackagetime;
    }

    public void setLastpackagetime(String lastpackagetime) {
        this.lastpackagetime = lastpackagetime;
    }

    public List<SensorErrnoBean> getSensor_errno() {
        return sensor_errno;
    }

    public void setSensor_errno(List<SensorErrnoBean> sensor_errno) {
        this.sensor_errno = sensor_errno;
    }

}
