package com.shmedo.mcloudapp.maps.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.model.PoiSearchType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/15 <br/>
 * 描述：    地图头部搜索框
 */
public class MapSearchView extends RelativeLayout {
    public static final int SEARCH_WITH_EMPTY = 0x0010;//搜索框没有输入内容
    public static final int SEARCH_WITH_INPUT_TEXT = 0x0011;//搜索框填充了搜索内容

    @BindView(R.id.ll_search_normal)
    View llSearchNormal;

    @BindView(R.id.ll_search_with_poi_input)
    View llSearchWithPoiInput;

    @BindView(R.id.iv_leave)
    FloatingActionButton fabBack;

    @BindView(R.id.tv_poi_title)
    TextView tvPoiTitle;

    private int mode = SEARCH_WITH_EMPTY;

    private PoiSearchType searchType;

    private OnMapHeadSearchViewClickListener mListener;


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
        llSearchNormal.setVisibility(View.VISIBLE);
        llSearchWithPoiInput.setVisibility(View.GONE);
    }

    @OnClick({R.id.iv_leave, R.id.tv_search_normal, R.id.tv_search_by_latlong, R.id.iv_goback, R.id.tv_poi_title, R.id.tv_cancel_poi})
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.iv_leave:
                mListener.onLeaveMapClick();
                break;

            case R.id.iv_goback:
                if (searchType != null && searchType == PoiSearchType.LATLNG_SEARCH) {
                    mListener.onCancelPoiSearchClick();
                } else {
                    mListener.onSearchNormalClick();
                }
                break;

            case R.id.tv_search_normal:
                mListener.onSearchNormalClick();
                break;

            case R.id.tv_search_by_latlong:
                mListener.onSearchLatLongClick();
                break;

            case R.id.tv_poi_title:
                mListener.onGoBackSearchListClick();
                break;

            case R.id.tv_cancel_poi:
                mListener.onCancelPoiSearchClick();
                break;
        }
    }

    public int getSearchMode() {
        return mode;
    }

    public void setSearchMode(int mode) {
        this.mode = mode;

        if (mode == SEARCH_WITH_EMPTY) {
            llSearchNormal.setVisibility(View.VISIBLE);
            llSearchWithPoiInput.setVisibility(View.GONE);
            tvPoiTitle.setText("");
        } else if (mode == SEARCH_WITH_INPUT_TEXT) {
            llSearchNormal.setVisibility(View.GONE);
            llSearchWithPoiInput.setVisibility(View.VISIBLE);
        }
    }

    public PoiSearchType getSearchType(){
        return searchType;
    }

    public void setSearchType(PoiSearchType searchType) {
        this.searchType = searchType;
    }


    public String getPoiInputText() {
        String text = tvPoiTitle.getText().toString();

        return text;
    }

    public void setPoiInputText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        tvPoiTitle.setText(text);
    }

    public void setOnMapHeaderViewClickListener(OnMapHeadSearchViewClickListener listener) {
        this.mListener = listener;
    }

    /**
     * MapHeaderView点击监听
     */
    public interface OnMapHeadSearchViewClickListener {
        /**
         * 点击返回，退出地图页面
         */
        void onLeaveMapClick();

        /**
         * 点击常规搜索
         */
        void onSearchNormalClick();

        /**
         * 点击按经纬度搜索点位
         */
        void onSearchLatLongClick();

        /**
         * 点击返回 poi 搜索列表页面
         */
        void onGoBackSearchListClick();

        /**
         * 点击取消按钮，恢复到显示正常搜索框
         */
        void onCancelPoiSearchClick();
    }
}

