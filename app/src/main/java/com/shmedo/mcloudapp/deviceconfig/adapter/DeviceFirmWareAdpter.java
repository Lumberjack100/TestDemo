package com.shmedo.mcloudapp.deviceconfig.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/1 <br/>
 * 描述：      固件信息适配器
 */
public class DeviceFirmWareAdpter extends BaseQuickAdapter<FirmWareInfo, BaseViewHolder> implements LoadMoreModule {
    public DeviceFirmWareAdpter(@Nullable List<FirmWareInfo> data) {
        super(R.layout.item_device_firmware_info, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, FirmWareInfo firmWareInfo) {
        holder.setText(R.id.tv_versionName, firmWareInfo.getFwName() + "(" + firmWareInfo.getFwVersion() + ")");

        int position = getItemPosition(firmWareInfo);
        if (position == 0) {
            holder.setGone(R.id.tv_latest_flag, false);
        } else {
            holder.setGone(R.id.tv_latest_flag, true);
        }

        if (firmWareInfo.isChecked()) {
            holder.setTextColorRes(R.id.tv_versionName, R.color.text_color_343434);
            holder.setGone(R.id.iv_checked_flag, false);
        } else {
            holder.setTextColorRes(R.id.tv_versionName, R.color.text_color_666666);
            holder.setGone(R.id.iv_checked_flag, true);
        }
    }
}
