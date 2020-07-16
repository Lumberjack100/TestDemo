package com.shmedo.mcloudapp.maps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.shmedo.mcloudapp.R;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/16 <br/>
 * 描述：    工具箱
 */
public class MapToolView extends BaseIconView{
    public MapToolView(Context context) {
        this(context, null);
    }

    public MapToolView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapToolView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Timber.d("width=" + getMeasuredWidth() + ",height=" + getMeasuredHeight());
    }

    @Override
    public boolean createBackground() {
        setBackgroundResource(R.drawable.icon_up_selector);
        return true;
    }

    @Override
    public boolean createIcon() {
        setIconResource(R.drawable.ic_map_tool);
        setText("工具箱");
        return true;
    }
}
