package com.shmedo.mcloudapp.projects.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/18 <br/>
 * 描述：     TODO
 */
public class DeviceInfoAdapter extends BaseQuickAdapter<ProjectDeviceInfo, BaseViewHolder> {
    public DeviceInfoAdapter(@Nullable List<ProjectDeviceInfo> data) {
        super(R.layout.item_device_info, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProjectDeviceInfo projectDeviceInfo) {
        holder.setText(R.id.tv_device_sn, projectDeviceInfo.getName());
        holder.setText(R.id.tv_device_type, projectDeviceInfo.getDeviceTypeName());

        if (projectDeviceInfo.isOnline()) {
            holder.setTextColorRes(R.id.tv_device_sn, R.color.title_text_color);
        } else {
            holder.setTextColorRes(R.id.tv_device_sn, R.color.text_color_gray_b3b3b3);

        }

    }
}
