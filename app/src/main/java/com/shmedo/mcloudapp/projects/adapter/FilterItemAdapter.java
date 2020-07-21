package com.shmedo.mcloudapp.projects.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.model.FilterItem;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     TODO
 */
public class FilterItemAdapter extends BaseQuickAdapter<FilterItem, BaseViewHolder> {
    public FilterItemAdapter() {
        super(R.layout.item_project_filter_button);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, @org.jetbrains.annotations.Nullable FilterItem filterItem) {
        holder.setText(R.id.title, filterItem.getName());

        if (filterItem.isChecked()) {
            holder.setBackgroundResource(R.id.title, R.drawable.checkable_btn_bg_checked);
        } else {
            holder.setBackgroundResource(R.id.title, R.drawable.checkable_btn_bg_normal);
        }
    }
}
