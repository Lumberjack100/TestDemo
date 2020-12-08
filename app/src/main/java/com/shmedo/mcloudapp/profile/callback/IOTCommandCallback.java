package com.shmedo.mcloudapp.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：     TODO
 */
public interface IOTCommandCallback {

    void onResponseReceived(@NonNull final BluetoothDevice device, final String result);
}
