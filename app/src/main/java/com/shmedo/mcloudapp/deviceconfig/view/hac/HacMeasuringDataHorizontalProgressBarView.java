package com.shmedo.mcloudapp.deviceconfig.view.hac;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.constant.RegexConstants;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.shmedo.mcloudapp.R;

import java.text.DecimalFormat;
import java.util.Locale;

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

    private DecimalFormat decimalFormat = new DecimalFormat("#");


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

        int progress = 0;
        try {
            String[] values = measurePoint.split("\\|");
            if (!TextUtils.isEmpty(values[1]) && RegexUtils.isMatch(RegexConstants.REGEX_INTEGER, values[1])) {
                horizontalBar.setMax(Integer.parseInt(values[1]));
            }
            if (!TextUtils.isEmpty(values[0])) {
                progress = Integer.parseInt(values[0]);
                horizontalBar.setProgress(progress, true);
            }
            mTvDataNum.setText(String.format("(%s/%s)", values[0], values[1]));
            float result = (horizontalBar.getMax() == 0) ? 0 : (float) progress / horizontalBar.getMax();
            mTvDataPercent.setText(String.format("%s%%", decimalFormat.format(result * 100)));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setMaxProgress() {
        horizontalBar.setProgress(horizontalBar.getMax(), true);
        mTvDataNum.setText(String.format(Locale.getDefault(), "(%d/%d)", horizontalBar.getMax(), horizontalBar.getMax()));
        mTvDataPercent.setText("100%");
    }

    public void setProgressDrawable(boolean isReadData) {
        horizontalBar.setProgressDrawable(isReadData ? ResourceUtils.getDrawable(R.drawable.custom_progress_horizontal_blue) : ResourceUtils.getDrawable(R.drawable.custom_progress_horizontal_green));
    }
}
