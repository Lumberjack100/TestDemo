package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.maps.util.ScreenShotAction;

import butterknife.BindView;
import butterknife.OnClick;

public class SpeedTestResultActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView pingTextView;

    @BindView(R.id.tv_delay)
    TextView mTvDelay;

    @BindView(R.id.tv_download_speed)
    TextView mTvDownloadSpeed;

    @BindView(R.id.tv_upload_speed)
    TextView mTvUploadSpeed;

    @BindView(R.id.tv_nettype)
    TextView mTvNetType;

    @BindView(R.id.tv_operator_name)
    TextView mTvOperatorName;

    @BindView(R.id.tv_location)
    TextView mTvLocation;

    @BindView(R.id.tv_gprs_signal)
    TextView mTvGpsSignal;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SpeedTestResultActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_speed_test_result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @OnClick({R.id.back, R.id.tv_screenshot})
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        } else if (v.getId() == R.id.tv_screenshot) {
            ScreenShotAction screenShotAction = new ScreenShotAction(this);
            screenShotAction.execute();
        }
    }
}
