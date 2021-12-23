package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DASSensorItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/12 <br/>
 * 描述：     TODO #gh#
 */
public class DASSensorAdapter extends BaseQuickAdapter<DASSensorItem, BaseViewHolder> {

    public DASSensorAdapter(@Nullable List<DASSensorItem> data) {
        super(R.layout.item_das_sensor, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, DASSensorItem dasSensorItem) {
        holder.setImageResource(R.id.iv_das_sensor, dasSensorItem.getResId());
        if (!TextUtils.isEmpty(dasSensorItem.getSensorAddress())) {
            try {
                String sensorAisle = dasSensorItem.isVibratingWireSensor() ? String.valueOf(Integer.parseInt(dasSensorItem.getSensorAddress()) + 1) : dasSensorItem.getSensorAddress();
                holder.setText(R.id.tv_address, sensorAisle);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
