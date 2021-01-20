package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/9 <br/>
 * 描述：     TODO
 */
public class BleDeviceAdapter extends BaseQuickAdapter<DiscoveredBluetoothDevice, BaseViewHolder> {
    public BleDeviceAdapter() {
        super(R.layout.item_ble_device);
        setHasStableIds(true);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, DiscoveredBluetoothDevice bluetoothDevice) {
        holder.setText(R.id.tv_dev_name, TextUtils.isEmpty(bluetoothDevice.getName()) ? "Unknown device" : bluetoothDevice.getName());
        if (TextUtils.isEmpty(bluetoothDevice.getName())) {
            holder.setGone(R.id.tv_device_type, true);
        } else {
            holder.setGone(R.id.tv_device_type, false);
            if (bluetoothDevice.getName().endsWith("L")) {
                holder.setText(R.id.tv_device_type, "DAS");

            } else if (bluetoothDevice.getName().endsWith("T")) {
                if (bluetoothDevice.getName().startsWith("M20"))
                    holder.setText(R.id.tv_device_type, "M20");
                else
                    holder.setText(R.id.tv_device_type, "ADME");

            } else if (bluetoothDevice.getName().endsWith("V")) {
                holder.setText(R.id.tv_device_type, "M20");
            } else {
                holder.setText(R.id.tv_device_type, "UnKnown");
            }
        }
    }
}
