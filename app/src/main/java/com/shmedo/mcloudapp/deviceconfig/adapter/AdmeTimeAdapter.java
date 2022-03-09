package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.AdmeTimeItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/8 <br/>
 * 描述：     TODO
 */
public class AdmeTimeAdapter extends BaseQuickAdapter<AdmeTimeItem, BaseViewHolder> {
    public AdmeTimeAdapter(@Nullable List<AdmeTimeItem> data) {
        super(R.layout.item_time, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, AdmeTimeItem admeTimeItem) {
        holder.setGone(R.id.iv_add_time, !admeTimeItem.isAddButton());
        holder.setGone(R.id.tv_time, admeTimeItem.isAddButton());
        if (!TextUtils.isEmpty(admeTimeItem.getTime())) {
            holder.setText(R.id.tv_time, admeTimeItem.getTime());
        }
    }
}
