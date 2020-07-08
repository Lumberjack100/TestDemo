package com.shmedo.mcloudapp.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.widget.NestedScrollView;

import com.dragon.core.util.DeviceInfo;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.views
 * 创建者:   gonghe
 * 创建时间:  2019-12-18
 * 描述：    TODO
 */
public class MaxHeightNestedScrollView extends NestedScrollView {

    public MaxHeightNestedScrollView(Context context) {
        super(context);
    }

    public MaxHeightNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHeightNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight = DeviceInfo.getScreenHeight() * 4 / 5;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
