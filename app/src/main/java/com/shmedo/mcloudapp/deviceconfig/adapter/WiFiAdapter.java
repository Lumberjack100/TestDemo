package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.net.wifi.ScanResult;
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
public class WiFiAdapter extends BaseQuickAdapter<ScanResult, BaseViewHolder> {
    public WiFiAdapter() {
        super(R.layout.item_wifi_scanresult);
    }


    @Override
    protected void convert(@NotNull BaseViewHolder holder, ScanResult scanResult) {
        holder.setText(R.id.tv_dev_name, TextUtils.isEmpty(scanResult.SSID) ? "Unknown device" : scanResult.SSID);
    }
}
