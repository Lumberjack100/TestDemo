package com.shmedo.mcloudapp.maps.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.model.MapLayerInfo;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/30 <br/>
 * 描述：    地图图层类型适配器
 */
public class MapTypeAdapter extends BaseQuickAdapter<MapLayerInfo, BaseViewHolder> implements LoadMoreModule {
    public MapTypeAdapter() {
        super(R.layout.item_map_layer);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, @org.jetbrains.annotations.Nullable MapLayerInfo mapLayerInfo) {
        holder.setBackgroundResource(R.id.iv_map_type, mapLayerInfo.getLayerThumbnail());
        holder.setText(R.id.tv_layer_name, mapLayerInfo.getLayerName());

        if (mapLayerInfo.isChecked()) {
            holder.setImageResource(R.id.iv_map_type, R.drawable.map_mode_cheked);
            holder.setTextColorRes(R.id.tv_layer_name, R.color.map_primary);
        } else {
            holder.setImageResource(R.id.iv_map_type, 0);
            holder.setTextColorRes(R.id.tv_layer_name, R.color.title_text_color);
        }
    }
}
