package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/17 <br/>
 * 描述：    TODO
 */
public class PoiDetailBottomView extends LinearLayout {
    @BindView(R.id.tv_poi_title)
    public TextView tvPoiTitle;

    @BindView(R.id.tv_poi_distance)
    public TextView tvPoiDistance;

    @BindView(R.id.tv_share)
    public TextView tvShare;

    @BindView(R.id.tv_navigation)
    public TextView tvNavi;

    @BindView(R.id.tv_route)
    public TextView tvRoute;

    private OnPoiDetailBottomClickListener mListener;


    public PoiDetailBottomView(Context context) {
        this(context, null);
    }

    public PoiDetailBottomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public PoiDetailBottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.poi_detail_bottom, this, true);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.iv_close, R.id.tv_share, R.id.tv_navigation, R.id.tv_route})
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.iv_close:
                mListener.onCloseClick();
                break;

            case R.id.tv_share:
                mListener.onShareClick();
                break;

            case R.id.tv_navigation:
                mListener.onNaviClick();
                break;

            case R.id.tv_route:
                mListener.onRouteClick();
                break;
        }
    }

    public void setOnPoiDetailBottomClickListener(OnPoiDetailBottomClickListener listener) {
        this.mListener = listener;
    }

    /**
     * PoiDetailBottomView点击监听
     */
    public interface OnPoiDetailBottomClickListener {
        /**
         * 关闭点位详情框
         */
        void onCloseClick();

        /**
         * 分享
         */
        void onShareClick();

        /**
         * 导航
         */
        void onNaviClick();

        /**
         * 路线
         */
        void onRouteClick();
    }

}
