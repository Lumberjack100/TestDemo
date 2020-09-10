package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/9 <br/>
 * 描述：     TODO
 */
public class BleDeviceAdapter extends BaseQuickAdapter<BluetoothDevice, BaseViewHolder> {
    public BleDeviceAdapter() {
        super(R.layout.item_ble_device);
    }


    @Override
    protected void convert(@NotNull BaseViewHolder holder, BluetoothDevice bluetoothDevice) {
        holder.setText(R.id.tv_dev_name, TextUtils.isEmpty(bluetoothDevice.getName()) ? "Unknown device" : bluetoothDevice.getName());
    }
}
