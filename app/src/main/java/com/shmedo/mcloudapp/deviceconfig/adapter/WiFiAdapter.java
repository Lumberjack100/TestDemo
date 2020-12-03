package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hacknife.wifimanager.IWifi;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/9 <br/>
 * 描述：     TODO
 */
public class WiFiAdapter extends BaseQuickAdapter<IWifi, BaseViewHolder> {
    public WiFiAdapter() {
        super(R.layout.item_wifi_scanresult);
    }


    @Override
    protected void convert(@NotNull BaseViewHolder holder, IWifi iWifi) {
        holder.setText(R.id.tv_name, iWifi.name());
        holder.setText(R.id.tv_desc, iWifi.description2());
        holder.setGone(R.id.tv_desc, TextUtils.isEmpty(iWifi.description2()));
        holder.setGone(R.id.iv_wifi_lock, !iWifi.isEncrypt());
    }
}
