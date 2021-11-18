package com.shmedo.mcloudapp.maps.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.maps.adapter.MapTypeAdapter;
import com.shmedo.mcloudapp.maps.model.MapLayerInfo;
import com.shmedo.mcloudapp.maps.model.MapType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/28 <br/>
 * 描述：    地图图层选择视图
 */
public class MapLayerDrawerView extends LinearLayout {
    private Context mContext;

    @BindView(R.id.recyclerViewLayer)
    RecyclerView mRecyclerView;

    private MapTypeAdapter mapTypeAdapter;

    private OnMapLayerItemClickListener mListener;

    public MapLayerDrawerView(Context context) {
        this(context, null);
    }

    public MapLayerDrawerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapLayerDrawerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.map_layer_navi_layout, this, true);
        ButterKnife.bind(this);
        mContext = context;
        initAdapter();
        initView();
    }

    private void initView() {
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
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
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

    @OnClick({R.id.offlineMapDownloadView, R.id.mapSettingView})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.offlineMapDownloadView:
               //在Activity页面调用startActvity启动离线地图组件
                mContext.startActivity(new Intent(mContext, com.amap.api.maps.offlinemap.OfflineMapActivity.class));
                break;

            case R.id.mapSettingView:

                break;
        }
    }


    public void setOnMapLayerItemClickListener(OnMapLayerItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnMapLayerItemClickListener {
        void onMapLayerItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, MapLayerInfo mapLayerInfo);
    }
}
