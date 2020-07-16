package com.shmedo.mcloudapp.maps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.shmedo.mcloudapp.R;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/16 <br/>
 * 描述：    拍照控件
 */
public class TakePhotoView extends BaseIconView{
    public TakePhotoView(Context context) {
        this(context, null);
    }

    public TakePhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TakePhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        setBackgroundResource(R.drawable.icon_down_selector);
        return true;
    }

    @Override
    public boolean createIcon() {
        setIconResource(R.drawable.ic_map_camera);
        setText("拍照");
        return true;
    }
}
