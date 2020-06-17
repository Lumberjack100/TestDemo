package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/15 <br/>
 * 描述：    地图缩放控件
 */
public class ZoomView extends LinearLayout {

    private OnZoomViewClickListener mListener;

    public ZoomView(Context context) {
        this(context, null);
    }

    public ZoomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_zoom, this, true);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.iv_zoom_in,R.id.iv_zoom_out})
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_zoom_in:
                mListener. onZoomInClick();
                break;

            case R.id.iv_zoom_out:
                mListener.onZoomOutClick();
                break;
        }
    }

    public void setOnZoomViewClickListener(OnZoomViewClickListener listener) {
        this.mListener = listener;
    }

    /**
     * MapHeaderView点击监听
     */
    public interface OnZoomViewClickListener {
        /**
         *  点击放大
         */
        void onZoomInClick();

        /**
         * 点击缩小
         */
        void onZoomOutClick();

    }
}
