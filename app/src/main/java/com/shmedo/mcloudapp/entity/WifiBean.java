package com.shmedo.mcloudapp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   WifiBean
 * 创建者:   dpc
 * 创建时间:  2019/1/24 11:41
 *
 */
public class WifiBean implements Comparable<WifiBean>, Parcelable {
    private String wifiName;
    private String level;
    private String state;  //已连接  正在连接  未连接 三种状态
    private String capabilities;//加密方式

    @Override
    public String toString() {
        return "WifiBean{" +
            "wifiName='" + wifiName + '\'' +
            ", level='" + level + '\'' +
            ", state='" + state + '\'' +
            ", capabilities='" + capabilities + '\'' +
            '}';
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public int compareTo(WifiBean o) {
        int level1 = Integer.parseInt(this.getLevel());
        int level2 = Integer.parseInt(o.getLevel());
        return level1 - level2;
    }


    @Override public int describeContents() { return 0; }


    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.wifiName);
        dest.writeString(this.level);
        dest.writeString(this.state);
        dest.writeString(this.capabilities);
    }


    public WifiBean() {}


    protected WifiBean(Parcel in) {
        this.wifiName = in.readString();
        this.level = in.readString();
        this.state = in.readString();
        this.capabilities = in.readString();
    }


    public static final Creator<WifiBean> CREATOR = new Creator<WifiBean>() {
        @Override public WifiBean createFromParcel(Parcel source) {return new WifiBean(source);}


        @Override public WifiBean[] newArray(int size) {return new WifiBean[size];}
    };
}