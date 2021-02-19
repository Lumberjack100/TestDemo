package com.shmedo.mcloudapp.projects.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.model.FilterItem;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     TODO #gh#
 */
public class FilterItemAdapter extends BaseQuickAdapter<FilterItem, BaseViewHolder> {
    public FilterItemAdapter() {
        super(R.layout.item_project_filter_button);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, @org.jetbrains.annotations.Nullable FilterItem filterItem) {
        holder.setText(R.id.tv_title, filterItem.getName());

        if (filterItem.isChecked()) {
            holder.setBackgroundResource(R.id.fl_filter_item, R.drawable.bg_project_filter_item_checked);
            holder.setVisible(R.id.iv_filter_tight,true);
            holder.setTextColorRes(R.id.tv_title,R.color.project_blue_color);
        } else {
            holder.setBackgroundResource(R.id.fl_filter_item, R.drawable.bg_project_filter_item_normal);
            holder.setVisible(R.id.iv_filter_tight,false);
            holder.setTextColorRes(R.id.tv_title,R.color.gray_808080);
        }
    }
}
