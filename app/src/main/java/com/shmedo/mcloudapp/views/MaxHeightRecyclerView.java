package com.shmedo.mcloudapp.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.mcloudapp.util.DensityUtil;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.views
 * 创建者:   gonghe
 * 创建时间:  2019-12-27
 * 描述：    TODO
 */
public class MaxHeightRecyclerView extends RecyclerView {
    public MaxHeightRecyclerView(Context context) {
        super(context);
    }

    public MaxHeightRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHeightRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight = DensityUtil.getDisplayMetrics().heightPixels * 3 / 5;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
