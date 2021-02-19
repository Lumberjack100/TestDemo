package com.shmedo.mcloudapp.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：     TODO #gh#
 */
public abstract class IOTCommandDataCallback implements ProfileDataCallback, IOTCommandCallback {

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() < 5) {
            onInvalidDataReceived(device, data);
            return;
        }

        String result = data.getStringValue(0);
        onResponseReceived(device, result);
    }
}
