package com.shmedo.mcloudapp.deviceconfig.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/3 <br/>
 * 描述：     传感器错误信息
 */
public class SensorErrnoBean implements Parcelable {
    /**
     * errno : 0
     * sensor_id : 3
     */

    private int errno;
    private String sensor_id;

    protected SensorErrnoBean(Parcel in) {
        errno = in.readInt();
        sensor_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(errno);
        dest.writeString(sensor_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SensorErrnoBean> CREATOR = new Creator<SensorErrnoBean>() {
        @Override
        public SensorErrnoBean createFromParcel(Parcel in) {
            return new SensorErrnoBean(in);
        }

        @Override
        public SensorErrnoBean[] newArray(int size) {
            return new SensorErrnoBean[size];
        }
    };

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(String sensor_id) {
        this.sensor_id = sensor_id;
    }


}
