package com.shmedo.mcloudapp.deviceconfig.view.hac;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/29 <br/>
 * 描述：     数据读取/上传阶段动态进度展示控件
 */
public class HacMeasuringDataHorizontalProgressBarView extends LinearLayout {
    @BindView(R.id.tv_data_num)
    TextView mTvDataNum;//当前处理数据个数

    @BindView(R.id.tv_data_percent)
    TextView mTvDataPercent;//当前处理百分比

    @BindView(R.id.horizontalBar)
    ProgressBar horizontalBar;//

    public HacMeasuringDataHorizontalProgressBarView(Context context) {
        this(context, null);
    }

    public HacMeasuringDataHorizontalProgressBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HacMeasuringDataHorizontalProgressBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.hac_measuringdata_horizotal_progressbar_view, this, true);
        ButterKnife.bind(this);
    }

    public void updateProgress(String measurePoint) {
        if (TextUtils.isEmpty(measurePoint) || !measurePoint.contains("|"))
            return;

        int maxValue = 0;
        int progress = 0;
        try {
            String[] values = measurePoint.split("\\|");
            if (!TextUtils.isEmpty(values[1])) {
                maxValue = Integer.parseInt(values[1]);
                horizontalBar.setMax(maxValue);
            }

            if (!TextUtils.isEmpty(values[0])) {
                progress = Integer.parseInt(values[1]);
                horizontalBar.setProgress(progress, true);
            }

            double result = maxValue == 0 ? 0 : (float) progress / maxValue;
            result = result * 100;
            mTvDataPercent.setText(String.format("%s%", result));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
