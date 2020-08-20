package com.shmedo.mcloudapp.bluetooth;

import com.shmedo.mcloudapp.deviceconfig.model.MDevice;

import java.util.List;

/**
 * Created by Liudongdong on 18/2/1.
 */

public class BluetoothDeviceFindEventData {
    private MDevice newDevice;
    private List<MDevice> allDevice;

    public BluetoothDeviceFindEventData(MDevice newDevice, List<MDevice> allDevice) {
        this.newDevice = newDevice;
        this.allDevice = allDevice;
    }

    public MDevice getNewDevice() {
        return newDevice;
    }

    public List<MDevice> getAllDevice() {
        return allDevice;
    }
}
