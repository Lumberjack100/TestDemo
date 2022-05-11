package com.shmedo.mcloudapp.deviceconfig.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.CommonLogInfo;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/5/11 <br/>
 * 描述：     TODO
 */
public class CommonLogAdapter extends BaseQuickAdapter<CommonLogInfo, BaseViewHolder> {

    public CommonLogAdapter(@Nullable List<CommonLogInfo> data) {
        super(R.layout.item_common_log, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, CommonLogInfo commonLogInfo) {
        holder.setText(R.id.tv_log_time, commonLogInfo.getLogTime());
        holder.setText(R.id.tv_log_content, commonLogInfo.getLogContent());
        holder.setTextColorRes(R.id.tv_log_content, commonLogInfo.getColor() == 0 ? R.color.title_text_color : commonLogInfo.getColor());
    }
}
