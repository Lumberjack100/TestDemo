package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.model.MapLayerInfo;
import com.shmedo.mcloudapp.maps.model.MapType;
import com.shmedo.mcloudapp.util.DensityUtil;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.GridSpacingItemDecoration;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/28 <br/>
 * 描述：    地图图层选择视图
 */
public class NaviMapLayerView extends LinearLayout {
    private Context mContext;

    @BindView(R.id.recyclerViewLayer)
    RecyclerView mRecyclerView;

    private MapTypeAdapter mapTypeAdapter;

    private List<MapLayerInfo> mapLayerInfoList;

    private OnMapLayerItemClickListener mListener;

    public NaviMapLayerView(Context context) {
        this(context, null);
    }

    public NaviMapLayerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NaviMapLayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.map_layer_navi_layout, this, true);
        ButterKnife.bind(this);
        mContext = context;
        initAdapter();
        initView();
    }

    private void initView() {
        mapLayerInfoList = new ArrayList<>();
        MapLayerInfo mapLayerInfo = new MapLayerInfo();
        mapLayerInfo.setLayerName("标准地图");
        mapLayerInfo.setLayerThumbnail(R.drawable.map_mode_plain_normal);
        mapLayerInfo.setMapType(MapType.GAODE_NORMAL);
        mapTypeAdapter.addData(mapLayerInfo);

        mapLayerInfo = new MapLayerInfo();
        mapLayerInfo.setLayerName("卫星地图");
        mapLayerInfo.setLayerThumbnail(R.drawable.map_mode_satellite_normal);
        mapLayerInfo.setMapType(MapType.GAODE_SATELLITE);
        mapLayerInfo.setChecked(true);
        mapTypeAdapter.addData(mapLayerInfo);
    }

    private void initAdapter() {
        mapTypeAdapter = new MapTypeAdapter();
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mContext, 15);//每一个矩形的间距
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        mRecyclerView.setAdapter(mapTypeAdapter);
        mapTypeAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                MapLayerInfo mapLayerInfo = mapTypeAdapter.getItem(position);
                if (mapLayerInfo.isChecked())
                    return;

                for (MapLayerInfo bean : mapTypeAdapter.getData()) {
                    bean.setChecked(false);
                }
                mapLayerInfo.setChecked(true);
                mapTypeAdapter.notifyDataSetChanged();

                if (mListener != null) {
                    mListener.onMapLayerItemClick(adapter, view, mapLayerInfo);
                }
            }
        });
    }


    public void setOnMapLayerItemClickListener(OnMapLayerItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnMapLayerItemClickListener {

        void onMapLayerItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, MapLayerInfo mapLayerInfo);
    }

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
                holder.setTextColorRes(R.id.tv_layer_name, R.color.black_333333);
            }
        }
    }
}
