package com.shmedo.configlibrary.iot.model;

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

    private Integer id;//传感器通道号
    private String name;//传感器名称
    private Integer errno;//错误码
    private Integer val;//值


    protected SensorErrnoInfo(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        if (in.readByte() == 0) {
            errno = null;
        } else {
            errno = in.readInt();
        }
        if (in.readByte() == 0) {
            val = null;
        } else {
            val = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(name);
        if (errno == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(errno);
        }
        if (val == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(val);
        }
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getErrno() {
        return errno;
    }

    public void setErrno(Integer errno) {
        this.errno = errno;
    }

    public Integer getVal() {
        return val;
    }

    public void setVal(Integer val) {
        this.val = val;
    }
}
