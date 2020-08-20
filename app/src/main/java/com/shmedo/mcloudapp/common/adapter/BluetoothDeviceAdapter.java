package com.shmedo.mcloudapp.common.adapter;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.MDevice;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.adapter
 * 创建者:   gonghe
 * 创建时间:  2020-02-27
 * 描述：    蓝牙设备列表适配器类
 */
public class BluetoothDeviceAdapter extends BaseQuickAdapter<MDevice, BaseViewHolder> {

    public BluetoothDeviceAdapter(int layoutResId, @Nullable List<MDevice> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, MDevice mDevice) {
        Timber.d("This is an Item, pos: " + (holder.getAdapterPosition() - getHeaderLayoutCount()));

        BluetoothDevice bluetoothDevice = mDevice.getDevice();
        holder.setText(R.id.tv_dev_name, TextUtils.isEmpty(bluetoothDevice.getName()) ? "N/A" : bluetoothDevice.getName());
        holder.setText(R.id.tv_dev_mac, bluetoothDevice.getAddress());
        holder.setText(R.id.tv_dev_signal, mDevice.getRssi() + "dBm");
    }
}
