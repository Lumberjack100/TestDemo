package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/17 <br/>
 * 描述：    TODO
 */
public class PoiDetailBottomView extends LinearLayout {
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
    }
}
