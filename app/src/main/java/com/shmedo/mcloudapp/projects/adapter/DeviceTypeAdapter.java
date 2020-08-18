package com.shmedo.mcloudapp.projects.adapter;

import android.graphics.Typeface;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.model.DeviceTypeInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/18 <br/>
 * 描述：    设备类型适配器
 */
public class DeviceTypeAdapter extends BaseQuickAdapter<DeviceTypeInfo, BaseViewHolder> {

    public DeviceTypeAdapter(@Nullable List<DeviceTypeInfo> data) {
        super(R.layout.item_device_type, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, DeviceTypeInfo deviceTypeInfo) {
        holder.setText(R.id.tv_name, deviceTypeInfo.getName());

        if (deviceTypeInfo.getName().equals("全部")) {
            holder.setGone(R.id.tv_num, true);
        } else {
            holder.setGone(R.id.tv_num, false);
            holder.setText(R.id.tv_num, "(" + deviceTypeInfo.getNum() + ")");
        }

        TextView tvName = (TextView) holder.getView(R.id.tv_name);
        TextView tvNum = (TextView) holder.getView(R.id.tv_num);
        if (deviceTypeInfo.isChecked()) {
            holder.setBackgroundResource(R.id.ll_item, R.drawable.bg_device_type_checked);
            holder.setTextColorRes(R.id.tv_name, R.color.title_text_color);
            tvName.setTextSize(17);
            tvNum.setTextSize(17);
            tvName.setTypeface(tvName.getTypeface(), Typeface.BOLD);

        } else {
            holder.setBackgroundResource(R.id.ll_item, 0);
            holder.setTextColorRes(R.id.tv_name, R.color.text_color_gray_666666);
            tvName.setTextSize(15);
            tvNum.setTextSize(15);
            tvName.setTypeface(tvName.getTypeface(), Typeface.NORMAL);
        }
    }
}
