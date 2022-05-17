package com.shmedo.mcloudapp.deviceconfig.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCloudDataInfo;

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
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(JsonParser.parseString(info.getContent()));
            holder.setText(R.id.tv_data_content, prettyJson);
        } catch (Exception ex) {
            ex.printStackTrace();
            holder.setText(R.id.tv_data_content, info.getContent());
        }
    }
}
