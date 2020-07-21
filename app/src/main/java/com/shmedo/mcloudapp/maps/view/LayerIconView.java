package com.shmedo.mcloudapp.maps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.shmedo.mcloudapp.R;

import timber.log.Timber;


/**
 * 图层
 */
public class LayerIconView extends BaseIconView {
    public LayerIconView(Context context) {
        this(context, null);
    }

    public LayerIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LayerIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
