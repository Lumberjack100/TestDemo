package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.maps.ui.activity.SpeedTestActivity;
import com.shmedo.mcloudapp.maps.ui.activity.SpeedTestResultActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/22 <br/>
 * 描述：    TODO
 */
public class NaviToolView extends LinearLayout {
    private Context mContext;

    private OnNaviToolViewClickListener mListener;

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
        mContext=context;
    }


    @OnClick({R.id.testSpeedView,R.id.measureDistanceView, R.id.calculatedAreaView, R.id.compassView})
    public void onClick(View view) {
//        if (mListener == null) {
//            return;
//        }

        switch (view.getId()) {
            case R.id.testSpeedView:
//                mListener.onTestSpeedClick();
                SpeedTestActivity.startActivity(mContext);
                break;

            case R.id.measureDistanceView:
                SpeedTestResultActivity.startActivity(mContext);
//                mListener.onMeasureDistanceClick();
                break;

            case R.id.calculatedAreaView:
//                mListener.onCalculatedAreaClick();
                break;

            case R.id.compassView:
//                mListener.onCompassClick();
                break;
        }
    }

    public void setOnNaviToolViewClickListener(OnNaviToolViewClickListener listener) {
        this.mListener = listener;
    }

    public interface OnNaviToolViewClickListener {
        /**
         * 网络测速
         */
        void onTestSpeedClick();

        /**
         * 测距
         */
        void onMeasureDistanceClick();

        /**
         * 计算面积
         */
        void onCalculatedAreaClick();

        /**
         * 指南针
         */
        void onCompassClick();

    }
}
