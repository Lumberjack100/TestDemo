package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.maps.model.NetWorkQuality;
import com.shmedo.mcloudapp.maps.util.ScreenShotAction;
import com.shmedo.mcloudapp.util.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

public class SpeedTestResultActivity extends BaseActivity {
    private static final String RESULT_PARAM = "result_param";

    @BindView(R.id.tv_title)
    TextView mTvTitle;

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

    private NetWorkQuality netWorkQuality;

    public static void startActivity(Context context, NetWorkQuality netWorkQuality) {
        Intent intent = new Intent(context, SpeedTestResultActivity.class);
        intent.putExtra(RESULT_PARAM, netWorkQuality);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_speed_test_result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        mTvTitle.setText(sdf.format(new Date()));
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            netWorkQuality = (NetWorkQuality) intent.getSerializableExtra(RESULT_PARAM);
            if (netWorkQuality != null) {
                mTvDelay.setText(netWorkQuality.getDelay().replace(" ","\n"));
                mTvDownloadSpeed.setText(netWorkQuality.getDownloadSpeed().replace(" ","\n"));
                mTvUploadSpeed.setText(netWorkQuality.getUploadSpeed().replace(" ","\n"));
            }
        }


        showNetType();
        showOperatorName();
    }

    private void showNetType() {
        String netType = "";
        NetworkUtils.NetworkType networkType = NetworkUtils.getNetworkType();
        switch (networkType) {
            case NETWORK_WIFI:
                netType = "WiFi网络";
                break;

            case NETWORK_4G:
                netType = "4G网络";
                break;

            case NETWORK_3G:
                netType = "3G网络";
                break;

            case NETWORK_2G:
                netType = "2G网络";
                break;

            default:
                netType = "未知网络类型";
                break;
        }

        mTvNetType.setText(netType);
    }

    private void showOperatorName() {
        String name="";
        int opeType = NetworkUtils.getCellularOperatorType();
        switch (opeType) {
            case 0:
                name="other";
                break;

            case 1:
                name="中国移动";
                break;

            case 2:
                name="中国联通";
                break;

            case 3:
                name="中国电信";
                break;

            case -1:
                name="无sim卡";
                break;

            case -2:
                name="数据流量未打开";
                break;

            default:
                break;
        }
        mTvOperatorName.setText(name);
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
