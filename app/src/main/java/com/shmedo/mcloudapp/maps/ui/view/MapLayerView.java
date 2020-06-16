package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.shmedo.mcloudapp.R;

import timber.log.Timber;


/**
 * 图层
 */
public class MapLayerView extends BaseIconView {
    public MapLayerView(Context context) {
        this(context, null);
    }

    public MapLayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapLayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Timber.d("width=" + getMeasuredWidth() + ",height=" + getMeasuredHeight());
    }

    @Override
    public boolean createBackground() {
        setBackgroundResource(R.drawable.icon_middle_selector);
        return true;
    }

    @Override
    public boolean createIcon() {
        setIconResource(R.drawable.ic_map_layer);
        setText("图层");
        return true;
    }
}
