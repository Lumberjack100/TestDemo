package com.shmedo.mcloudapp.entity.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015-11-16.
 */
public class MDevice implements Parcelable {
    private BluetoothDevice device;
    private int rssi;

    public MDevice(){

    }


    protected MDevice(Parcel in) {
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        rssi = in.readInt();
    }


    public static final Creator<MDevice> CREATOR = new Creator<MDevice>() {
        @Override
        public MDevice createFromParcel(Parcel in) {
            return new MDevice(in);
        }


        @Override
        public MDevice[] newArray(int size) {
            return new MDevice[size];
        }
    };


    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }


    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public MDevice(BluetoothDevice device, int rssi) {

        this.device = device;
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MDevice) {
            return device.equals(((MDevice) o).getDevice());
        }
        return false;
    }


    @Override public int describeContents() {
        return 0;
    }


    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(device, flags);
        dest.writeInt(rssi);
    }
}
