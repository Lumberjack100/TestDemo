package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;

import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/22 <br/>
 * 描述：    工具箱视图
 */
public class NaviToolView extends LinearLayout {
    private Context mContext;

    public NaviToolView(Context context) {
        this(context, null);
    }

    public NaviToolView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NaviToolView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.toolbox_navi_layout, this, true);
        ButterKnife.bind(this);
        mContext = context;
    }

}
