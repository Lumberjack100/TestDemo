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
    public DeviceReportDataAdapter(@Nullable List<QueryCloudDataInfo> data) {
        super(R.layout.item_device_report_data, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, QueryCloudDataInfo info) {
        holder.setText(R.id.tv_data_time, info.getTimeStr());
        try {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();

//            String content = "{\"20001_1\":{\"device_id\":\"13230\",\"time\":\"2022-07-20 17:32:05\",\"level\":\"warn\",\"msg\":\"iot_cmd:$cmd=md_getworkmode \"}}\u0000\u0000\u0000";
            String content = info.getContent();
            content = content.replace("\u0000", ""); // removes NUL chars
            content = content.replace("\\u0000", ""); // removes backslash+u0000
            String prettyJson = gson.toJson(JsonParser.parseString(content));
            holder.setText(R.id.tv_data_content, prettyJson);
        } catch (Exception ex) {
            ex.printStackTrace();
            holder.setText(R.id.tv_data_content, info.getContent());
        }
    }
}
