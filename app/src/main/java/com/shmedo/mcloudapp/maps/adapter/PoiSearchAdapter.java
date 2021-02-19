package com.shmedo.mcloudapp.maps.adapter;

import com.amap.api.services.core.PoiItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/30 <br/>
 * 描述：     TODO #gh#
 */
public class PoiSearchAdapter extends BaseQuickAdapter<PoiItem, BaseViewHolder> implements LoadMoreModule {
    public PoiSearchAdapter() {
        super(R.layout.poi_search_adapter_item);
        addChildClickViewIds(R.id.iv_route);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, @org.jetbrains.annotations.Nullable PoiItem poiItem) {
        holder.setText(R.id.tv_search_title, poiItem.getTitle());
        holder.setText(R.id.tv_search_loc, poiItem.getSnippet());
    }
}
