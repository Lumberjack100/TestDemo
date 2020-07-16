package com.shmedo.mcloudapp.maps.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/15 <br/>
 * 描述：    地图头部搜索框
 */
public class MapSearchView extends RelativeLayout  {
    @BindView(R.id.iv_back)
    FloatingActionButton fabBack;

    private OnMapHeaderViewClickListener mListener;


    public MapSearchView(Context context) {
        this(context, null);
    }

    public MapSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_map_search, this, true);

        ButterKnife.bind(this);
        initView();
    }


    private void initView() {

    }

    @OnClick({R.id.iv_back,R.id.tv_search_normal, R.id.tv_search_LatLong})
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.iv_back:
                mListener.onBackClick();
                break;

            case R.id.tv_search_normal:
                mListener.onSearchNormalClick();
                break;

            case R.id.tv_search_LatLong:
                mListener.onSearchLatLongClick();
                break;
        }
    }

    public void setOnMapHeaderViewClickListener(OnMapHeaderViewClickListener listener) {
        this.mListener = listener;
    }

    /**
     * MapHeaderView点击监听
     */
    public interface OnMapHeaderViewClickListener {
        /**
         * 点击返回
         */
        void onBackClick();

        /**
         * 点击常规搜索
         */
        void onSearchNormalClick();

        /**
         * 点击按经纬度搜索点位
         */
        void onSearchLatLongClick();

    }
}

