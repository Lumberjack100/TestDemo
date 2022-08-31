package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/9 <br/>
 * 描述：     TODO #gh#
 */
public class BleDeviceAdapter extends BaseQuickAdapter<DiscoveredBluetoothDevice, BaseViewHolder> {
    public BleDeviceAdapter() {
        super(R.layout.item_ble_device);
        setHasStableIds(true);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void convert(@NotNull BaseViewHolder holder, DiscoveredBluetoothDevice bluetoothDevice) {
        holder.setText(R.id.tv_dev_name, TextUtils.isEmpty(bluetoothDevice.getDevice().getName()) ? "Unknown device" : bluetoothDevice.getDevice().getName());
        holder.setText(R.id.tv_mac_address, bluetoothDevice.getAddress());
        holder.setText(R.id.tv_rssi, bluetoothDevice.getRssi() + " dBm");
        if (TextUtils.isEmpty(bluetoothDevice.getDevice().getName())) {
            holder.setGone(R.id.tv_device_type, true);
        } else {
            holder.setGone(R.id.tv_device_type, false);

            ProductType productType = ProductType.valueBySuffix(bluetoothDevice.getDevice().getName());
            holder.setText(R.id.tv_device_type, productType.getPrefix());
        }
    }
}
