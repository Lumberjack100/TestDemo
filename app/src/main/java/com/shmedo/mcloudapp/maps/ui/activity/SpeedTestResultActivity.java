package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.shmedo.core.util.NetworkUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.LocationViewModel;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.mcloudapp.maps.model.NetWorkQuality;
import com.shmedo.mcloudapp.maps.util.ScreenShotAction;
import com.shmedo.mcloudapp.util.LocationUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class SpeedTestResultActivity extends BaseActivity {
    private static final String RESULT_PARAM = "result_param";
    public static final int PERMISSION_CODE_GPS = 0x011;

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

    private LocationViewModel locationViewModel;

    public static void startActivity(Context context, NetWorkQuality netWorkQuality) {
        Intent intent = new Intent(context, SpeedTestResultActivity.class);
        intent.putExtra(RESULT_PARAM, netWorkQuality);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_speed_test_result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationViewModel=getApplicationScopeViewModel(LocationViewModel.class);
        locationViewModel.getSyncPositionBean().observeInActivity(this, new Observer<SyncPositionBean>() {
            @Override
            public void onChanged(SyncPositionBean syncPositionBean) {
                String latLong = String.format(Locale.getDefault(), "%.6f", syncPositionBean.getLongitude()) + "," + String.format(Locale.getDefault(), "%.6f", syncPositionBean.getLatitude());
                mTvLocation.setText(latLong);
                LocationUtils.getInstance().stopLocalService();
            }
        });

        initView();
        LocationUtils.getInstance().startLocalService();
    }

    private void initView() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        mTvTitle.setText(sdf.format(new Date()));
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            netWorkQuality = (NetWorkQuality) intent.getSerializableExtra(RESULT_PARAM);
            if (netWorkQuality != null) {
                mTvDelay.setText(netWorkQuality.getDelay().replace(" ", "\n"));
                mTvDownloadSpeed.setText(netWorkQuality.getDownloadSpeed().replace(" ", "\n"));
                mTvUploadSpeed.setText(netWorkQuality.getUploadSpeed().replace(" ", "\n"));
            }
        }
        showNetType();
        processOperatorName();
    }


    private void showNetType(){
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


    private void processOperatorName() {
        NetworkUtils.NetworkType networkType = NetworkUtils.getNetworkType();
        if (networkType == NetworkUtils.NetworkType.NETWORK_WIFI) {
            //获取连接的wifi名称
            mTvOperatorName.setText(NetworkUtils.getConnectWifiSsid());
        } else {
            showOperatorName();
        }
    }

    private void showOperatorName() {
        String name = "";
        int opeType = NetworkUtils.getCellularOperatorType();
        switch (opeType) {
            case 0:
                name = "other";
                break;

            case 1:
                name = "中国移动";
                break;

            case 2:
                name = "中国联通";
                break;

            case 3:
                name = "中国电信";
                break;

            case -1:
                name = "无sim卡";
                break;

            case -2:
                name = "数据流量未打开";
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


   /* *//**
     * 定位需要进行检测的权限数组
     *//*
    private String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    *//**
     * 获取连接WiFi的名称相关权限处理
     *//*
    private void checkWifiNeedPermissions() {
        //获取必要权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            needPermissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
        //获取必要权限
        AndPermission.with(this)
                .runtime()
                .permission(needPermissions)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        //先判断Android系统，9.0以上除了需要定位权限还需要开启GPS才能获取wifi名字
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            if (!LocationUtils.getInstance().isGpsEnabled()) {
                                showGPSSettingDialog();
                            } else {
                                //获取连接的wifi名称
                                mTvOperatorName.setText(NetworkUtils.getConnectWifiSsid());

                            }
                        } else {
                            //获取连接的wifi名称
                            mTvOperatorName.setText(NetworkUtils.getConnectWifiSsid());
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {

                    }
                })
                .start();
    }

    protected void showGPSSettingDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("权限申请").content("需要打开系统定位开关")
                .negativeText("暂不开启")
                .positiveText("去设置")
                .negativeColor(getResources().getColor(R.color.gray_797979))
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, PERMISSION_CODE_GPS);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case PERMISSION_CODE_GPS:
                if (LocationUtils.getInstance().isGpsEnabled()) {
//                    mTvOperatorName.setText(NetworkUtils.getConnectWifiSsid());
                }
                break;
        }
    }*/
}
