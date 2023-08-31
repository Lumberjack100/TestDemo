package com.shmedo.lib.device.base.iot_cmd.model.vms;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.shmedo.lib.device.base.iot_cmd.model.SensorErrnoInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：    Vms网关挂载的终端设备信息
 */
public class VmsTerminalInfo implements Parcelable {
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

    public static final int NORMAL_DEVICE = 1;
    public static final int SCAN_ADD_DEVICE = 2;
    private int itemType = NORMAL_DEVICE;

    private int asileNumber;//通道号
    private int netid;//网络号
    private int chl;//信道

    private String sn;//终端SN号
    private int addr;//终端地址
    private int uprssi;//上行信号强度
    private int downrssi;//下行信号强度
    private int tx;//发送数据
    private int rx;//接收数据
    private double volt;//终端电压
    private int status;//终端在线状态 0:离线，1：在线
    private String logintime;//注册时间
    private String lastpackagetime;//最后交互时间
    private List<SensorErrnoInfo> sensor;

    public VmsTerminalInfo(int itemType) {
        this.itemType = itemType;
    }

    protected VmsTerminalInfo(Parcel in) {
        itemType = in.readInt();
        asileNumber = in.readInt();
        netid = in.readInt();
        chl = in.readInt();
        sn = in.readString();
        addr = in.readInt();
        uprssi = in.readInt();
        downrssi = in.readInt();
        tx = in.readInt();
        rx = in.readInt();
        volt = in.readDouble();
        status = in.readInt();
        logintime = in.readString();
        lastpackagetime = in.readString();
        sensor = in.createTypedArrayList(SensorErrnoInfo.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(itemType);
        dest.writeInt(asileNumber);
        dest.writeInt(netid);
        dest.writeInt(chl);
        dest.writeString(sn);
        dest.writeInt(addr);
        dest.writeInt(uprssi);
        dest.writeInt(downrssi);
        dest.writeInt(tx);
        dest.writeInt(rx);
        dest.writeDouble(volt);
        dest.writeInt(status);
        dest.writeString(logintime);
        dest.writeString(lastpackagetime);
        dest.writeTypedList(sensor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VmsTerminalInfo> CREATOR = new Creator<VmsTerminalInfo>() {
        @Override
        public VmsTerminalInfo createFromParcel(Parcel in) {
            return new VmsTerminalInfo(in);
        }

        @Override
        public VmsTerminalInfo[] newArray(int size) {
            return new VmsTerminalInfo[size];
        }
    };

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getAsileNumber() {
        return asileNumber;
    }

    public void setAsileNumber(int asileNumber) {
        this.asileNumber = asileNumber;
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

    public double getVolt() {
        return volt;
    }

    public void setVolt(double volt) {
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

    public List<SensorErrnoInfo> getSensor() {
        return sensor == null ? new ArrayList<SensorErrnoInfo>() : sensor;
    }

    public void setSensor(List<SensorErrnoInfo> sensor) {
        this.sensor = sensor;
    }

}
