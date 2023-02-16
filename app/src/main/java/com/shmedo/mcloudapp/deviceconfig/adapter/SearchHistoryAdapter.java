package com.shmedo.mcloudapp.deviceconfig.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/2/15 <br/>
 * 描述：     TODO
 */
public class SearchHistoryAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public SearchHistoryAdapter(@Nullable List<String> data) {
        super(R.layout.item_search_history, data);
        setAnimationEnable(true);
        setAnimationWithDefault(BaseQuickAdapter.AnimationType.values()[1]);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, String item) {
        holder.setText(R.id.flow_tag, item);
    }
}
