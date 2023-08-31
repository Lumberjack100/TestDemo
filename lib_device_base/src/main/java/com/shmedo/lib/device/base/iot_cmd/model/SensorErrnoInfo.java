package com.shmedo.lib.device.base.iot_cmd.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：    传感器错误码实体
 */
public class SensorErrnoInfo implements Parcelable {
    /**
     * id : 0
     * name : 10005_1
     * errno : -4
     * val : 14213
     */
    private int id;//传感器通道号
    private String name;//传感器名称
    private int errno;//错误码
    private String val;//值
    private String in;//接入状态，0:未接入，1接入
    private String num;//编号


    protected SensorErrnoInfo(Parcel parcel) {
        id = parcel.readInt();
        name = parcel.readString();
        errno = parcel.readInt();
        val = parcel.readString();
        in = parcel.readString();
        num = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(errno);
        dest.writeString(val);
        dest.writeString(in);
        dest.writeString(num);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SensorErrnoInfo> CREATOR = new Creator<SensorErrnoInfo>() {
        @Override
        public SensorErrnoInfo createFromParcel(Parcel in) {
            return new SensorErrnoInfo(in);
        }

        @Override
        public SensorErrnoInfo[] newArray(int size) {
            return new SensorErrnoInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}
