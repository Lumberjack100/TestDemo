package com.shmedo.mcloudapp.deviceconfig.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.QueryCloudDataInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/4 <br/>
 * 描述：     设备上报数据
 */
public class DeviceReportDataAdapter extends BaseQuickAdapter<QueryCloudDataInfo, BaseViewHolder> {
    public DeviceReportDataAdapter( @Nullable List<QueryCloudDataInfo> data) {
        super(R.layout.item_device_report_data, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, QueryCloudDataInfo info) {
        holder.setText(R.id.tv_data_time, info.getTimeStr());
        holder.setText(R.id.tv_data_content, info.getContent());
    }
}
