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

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/29 <br/>
 * 描述：     数据测量阶段动态进度展示控件
 */
public class HacMeasuringDataVerticalProgressBarView extends LinearLayout {
    @BindView(R.id.tv_cur_depth)
    TextView mTvCurDepth;//

    @BindView(R.id.tv_hole_depth)
    TextView mTvHoleDepth;// 孔深

    @BindView(R.id.verticalBar)
    ProgressBar verticalBar;//

    private DecimalFormat decimalFormat = new DecimalFormat("#.#");


    public HacMeasuringDataVerticalProgressBarView(Context context) {
        this(context, null);
    }

    public HacMeasuringDataVerticalProgressBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HacMeasuringDataVerticalProgressBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.hac_measuringdata_vertical_progressbar_view, this, true);
        ButterKnife.bind(this);
    }

    public void init(String measurePoint) {
        if (TextUtils.isEmpty(measurePoint) || !measurePoint.contains("|"))
            return;

        try {
            String[] values = measurePoint.split("\\|");
            if (!TextUtils.isEmpty(values[1])) {
                double depth = Double.parseDouble(values[1]);
                mTvHoleDepth.setText(String.format("孔深 %s 米", decimalFormat.format(depth)));
                verticalBar.setMax((int) (depth * 10));
            }
            verticalBar.setProgress(0, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateProgress(String measurePoint) {
        if (TextUtils.isEmpty(measurePoint) || !measurePoint.contains("|"))
            return;

        try {
            String[] values = measurePoint.split("\\|");
            if (TextUtils.isEmpty(values[0]))
                verticalBar.setProgress(0, true);
            else {
                double value = Double.parseDouble(values[0]);
                mTvHoleDepth.setText(String.format("当前测得 %s 米", decimalFormat.format(value)));
                verticalBar.setProgress((int) (value * 10), true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setLastProgress(){
        verticalBar.setProgress( verticalBar.getMax(), true);
    }
}
