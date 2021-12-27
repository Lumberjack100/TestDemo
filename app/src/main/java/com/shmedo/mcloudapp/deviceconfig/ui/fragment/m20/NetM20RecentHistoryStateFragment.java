package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.shmedo.configlibrary.iot.model.m20.M20CurrentStateInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/1/21 <br/>
 * 描述：    M20网络模式 设备历史最近运行状态页面
 */
public class NetM20RecentHistoryStateFragment extends BaseFragment {
    public static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    /**
     * 基本信息
     */
    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_sim_card_number)
    TextView mTVSimCardNumber;

    @BindView(R.id.tv_imei_number)
    TextView mTvImeiNumber;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    @BindView(R.id.tv_board_type)
    TextView mTvBoardType;

    @BindView(R.id.tv_install_location)
    TextView mTvInstallLocation;

    @BindView(R.id.tv_storage_state)
    TextView mTvStorageState;

    @BindView(R.id.tv_continuous_running_time)
    TextView mTvContinuousRunningTime;

    /**
     * 通讯状态
     */
    @BindView(R.id.tv_device_star_num)
    TextView mTvDeviceStarNum;

    @BindView(R.id.tv_ams_connection_status)
    TextView mTvAmsConnectionStatus;

    @BindView(R.id.tv_4g_signal_strength)
    TextView mTv4gSignalStrength;

    @BindView(R.id.tv_link_one_status)
    TextView mTvLinkOneStatus;

    @BindView(R.id.tv_link_two_status)
    TextView mTvLinkTwoStatus;

    @BindView(R.id.tv_link_three_status)
    TextView mTvLinkThreeStatus;

    @BindView(R.id.tv_link_four_status)
    TextView mTvLinkFourStatus;

    /**
     * 设备工作信息
     */
    @BindView(R.id.tv_sensor_status)
    TextView mTvSensorStatus;

    @BindView(R.id.tv_inclination)
    TextView mTvInclination;

    @BindView(R.id.tv_internal_voltage)
    TextView mTvInternalVoltage;

    @BindView(R.id.tv_external_voltage)
    TextView mTvExternalVoltage;

    @BindView(R.id.tv_solar_panel_voltage)
    TextView mTvSolarPanelVoltage;

    @BindView(R.id.tv_ambient_temperature)
    TextView mTvAmbientTemprature;

    @BindView(R.id.tv_ambient_humidity)
    TextView mTvAmbientHumidity;

    @BindView(R.id.tv_supplementary_power)
    TextView mTvSupplementaryPower;

    @BindView(R.id.tv_power_consumption)
    TextView mTvPowerConsumption;

    private ProjectDeviceInfo projectDeviceInfo;

    private M20CurrentStateInfo m20CurrentStateInfo;

    public static NetM20RecentHistoryStateFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetM20RecentHistoryStateFragment fragment = new NetM20RecentHistoryStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_m20_recent_history_state_fragment;
    }


   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

}