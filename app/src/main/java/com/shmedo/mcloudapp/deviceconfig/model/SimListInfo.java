package com.shmedo.mcloudapp.deviceconfig.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/23 <br/>
 * 描述：     流量卡信息
 */
public class SimListInfo implements Parcelable {
    private int id;
    private String ccid;
    private String simNo;
    private String simIsp;
    private String vendor;
    private int deviceID;


    protected SimListInfo(Parcel in) {
        id = in.readInt();
        ccid = in.readString();
        simNo = in.readString();
        simIsp = in.readString();
        vendor = in.readString();
        deviceID = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(ccid);
        dest.writeString(simNo);
        dest.writeString(simIsp);
        dest.writeString(vendor);
        dest.writeInt(deviceID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SimListInfo> CREATOR = new Creator<SimListInfo>() {
        @Override
        public SimListInfo createFromParcel(Parcel in) {
            return new SimListInfo(in);
        }

        @Override
        public SimListInfo[] newArray(int size) {
            return new SimListInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCcid() {
        return ccid;
    }

    public void setCcid(String ccid) {
        this.ccid = ccid;
    }

    public String getSimNo() {
        return simNo;
    }

    public void setSimNo(String simNo) {
        this.simNo = simNo;
    }

    public String getSimIsp() {
        return simIsp;
    }

    public void setSimIsp(String simIsp) {
        this.simIsp = simIsp;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }
}
