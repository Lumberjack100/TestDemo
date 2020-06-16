package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/15 <br/>
 * 描述：    地图缩放控件
 */
public class ZoomView extends LinearLayout {

    public ZoomView(Context context) {
        this(context, null);
    }

    public ZoomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_zoom, this, true);
    }
}
