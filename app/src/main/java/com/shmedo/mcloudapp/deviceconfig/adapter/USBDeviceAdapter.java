package com.shmedo.mcloudapp.deviceconfig.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.USBDeviceItem;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/10 <br/>
 * 描述：     USB 设备列表适配器
 */
public class USBDeviceAdapter extends BaseQuickAdapter<USBDeviceItem, BaseViewHolder> {
    public USBDeviceAdapter() {
        super(R.layout.item_usb_device);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, USBDeviceItem usbDeviceItem) {
        if (usbDeviceItem.getDriver() == null) {
            holder.setText(R.id.tv_name, "<no driver>");

        } else if (usbDeviceItem.getDriver().getPorts().size() == 1) {
            holder.setText(R.id.tv_name, usbDeviceItem.getDriver().getClass().getSimpleName().replace("SerialDriver", ""));

        } else {
            holder.setText(R.id.tv_name, usbDeviceItem.getDriver().getClass().getSimpleName().replace("SerialDriver", "") + ", Port " + usbDeviceItem.getPort());
        }
        holder.setText(R.id.tv_desc, String.format(Locale.US, "Vendor %04X, Product %04X", usbDeviceItem.getDevice().getVendorId(), usbDeviceItem.getDevice().getProductId()));
    }
}
