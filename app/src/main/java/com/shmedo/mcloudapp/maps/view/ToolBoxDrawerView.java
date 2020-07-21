package com.shmedo.mcloudapp.maps.view;

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
 * 创建时间:  2020/6/22 <br/>
 * 描述：    工具箱视图
 */
public class ToolBoxDrawerView extends LinearLayout {
    private Context mContext;

    private OnMapToolItemClickListener mListener;


    public ToolBoxDrawerView(Context context) {
        this(context, null);
    }

    public ToolBoxDrawerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToolBoxDrawerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.toolbox_navi_layout, this, true);
        ButterKnife.bind(this);
        mContext = context;
    }

    @OnClick({R.id.testSpeedView, R.id.measureDistanceView, R.id.calculatedAreaView, R.id.compassView})
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.testSpeedView:
                mListener.onTestSpeedClick();
                break;

            case R.id.measureDistanceView:
                mListener.onMeasureDistanceClick();
                break;

            case R.id.calculatedAreaView:
                mListener.onCalculateAreaClick();
                break;

            case R.id.compassView:
                mListener.onCompassClick();
                break;
        }
    }

    public void setOnMapToolItemClickListener(OnMapToolItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnMapToolItemClickListener {
        /**
         * 网络测速
         */
        void onTestSpeedClick();

        /**
         * 测量距离
         */
        void onMeasureDistanceClick();

        /**
         * 测量面积
         */
        void onCalculateAreaClick();

        /**
         * 指南针
         */
        void onCompassClick();
    }

}
