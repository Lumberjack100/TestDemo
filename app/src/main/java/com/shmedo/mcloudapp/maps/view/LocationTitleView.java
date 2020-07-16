package com.shmedo.mcloudapp.maps.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/21 <br/>
 * 描述：    TODO
 */
public class LocationTitleView extends LinearLayout {
    private Context mContext;

    @BindView(R.id.tv_coordinate)
    TextView mTvCoordinate;

    @BindView(R.id.tv_altitude)
    TextView mTvAltitude;

    @BindView(R.id.tv_location_deviation)
    TextView mTvLocationDeviation;

    @BindView(R.id.tv_gps_signal)
    TextView mTvGpsSignal;


    public LocationTitleView(Context context) {
        this(context, null);
    }

    public LocationTitleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LocationTitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_location_title, this, true);
        ButterKnife.bind(this);
        mContext = context;
    }

    public void updataView(String coordinate, double altitude, float locationDeviation, int gpsSignal) {
        mTvCoordinate.setText(coordinate);
        mTvAltitude.setText(String.format("海拔 %sm", altitude));
        mTvLocationDeviation.setText(String.format("定位误差 %sm", locationDeviation));
        mTvGpsSignal.setText(String.valueOf(gpsSignal));
        mTvGpsSignal.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_signal_four, 0, 0, 0);

    }
}
