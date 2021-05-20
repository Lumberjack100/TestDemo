package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.m20.M20CurrentStateInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.BaseTcpIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/5/21 <br/>
 * 描述：     E40 TCP模式 设备运行状态页面
 */
public class TcpE40CurrentStateFragment extends BaseTcpIotCommunicateFragment {
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    /**
     * 基本信息
     */
//    @BindView(R.id.tv_device_sn)
//    TextView mTvDeviceSn;
//
//    @BindView(R.id.tv_sim_card_number)
//    TextView mTVSimCardNumber;
//
//    @BindView(R.id.tv_imei_number)
//    TextView mTvImeiNumber;
//
//    @BindView(R.id.tv_firmware_version)
//    TextView mTvFirmwareVersion;
//
//    @BindView(R.id.tv_board_type)
//    TextView mTvBoardType;
//
//    @BindView(R.id.tv_install_location)
//    TextView mTvInstallLocation;
//
//    @BindView(R.id.tv_storage_state)
//    TextView mTvStorageState;
//
//    @BindView(R.id.tv_continuous_running_time)
//    TextView mTvContinuousRunningTime;
//
//    /**
//     * 通讯状态
//     */
//    @BindView(R.id.tv_device_star_num)
//    TextView mTvDeviceStarNum;
//
//    @BindView(R.id.tv_phone_star_num)
//    TextView mTvPhoneStarNum;
//
//    @BindView(R.id.tv_ams_connection_status)
//    TextView mTvAmsConnectionStatus;
//
//    @BindView(R.id.tv_4g_signal_strength)
//    TextView mTv4gSignalStrength;
//
//    @BindView(R.id.tv_link_one_status)
//    TextView mTvLinkOneStatus;
//
//    @BindView(R.id.tv_link_two_status)
//    TextView mTvLinkTwoStatus;
//
//    @BindView(R.id.tv_link_three_status)
//    TextView mTvLinkThreeStatus;
//
//    @BindView(R.id.tv_link_four_status)
//    TextView mTvLinkFourStatus;
//
//    /**
//     * 设备工作信息
//     */
//    @BindView(R.id.tv_sensor_status)
//    TextView mTvSensorStatus;
//
//    @BindView(R.id.tv_inclination)
//    TextView mTvInclination;
//
//    @BindView(R.id.tv_internal_voltage)
//    TextView mTvInternalVoltage;
//
//    @BindView(R.id.tv_external_voltage)
//    TextView mTvExternalVoltage;
//
//    @BindView(R.id.tv_solar_panel_voltage)
//    TextView mTvSolarPanelVoltage;
//
//    @BindView(R.id.tv_ambient_temperature)
//    TextView mTvAmbientTemprature;
//
//    @BindView(R.id.tv_ambient_humidity)
//    TextView mTvAmbientHumidity;
//
//    @BindView(R.id.tv_supplementary_power)
//    TextView mTvSupplementaryPower;
//
//    @BindView(R.id.tv_power_consumption)
//    TextView mTvPowerConsumption;

    private M20CurrentStateInfo m20CurrentStateInfo;

    public static TcpE40CurrentStateFragment newInstance() {
        return new TcpE40CurrentStateFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.e40_current_state_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRefreshLayout();
        // 进入页面，刷新数据
        swipeRefresh.setRefreshing(true);
        queryStateInfo();
    }

    private void initRefreshLayout() {
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.refresh_failed_while_device_disconnected));
                    swipeRefresh.setRefreshing(false);
                    return;
                }
                queryStateInfo();
            }
        });
    }

    /**
     * 获取设备的当前状态
     */
    private void queryStateInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable(null, WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS);
        sendCommand(command);
    }

    @Override
    protected void doProgressRun() {
        super.doProgressRun();
        swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS: {
                swipeRefresh.setRefreshing(false);
                stopProgressRunnable();
                IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询设备状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                String content = commandResult.getResult();
                initStatusInfo(content);
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void initStatusInfo(String content) {
        try {
//            m20CurrentStateInfo = GsonFactory.getGson().fromJson(content, M20CurrentStateInfo.class);
//            if (m20CurrentStateInfo != null) {
//                mTvDeviceSn.setText(m20CurrentStateInfo.getSN());
//                mTVSimCardNumber.setText(m20CurrentStateInfo.getCCID());
//                mTvImeiNumber.setText(m20CurrentStateInfo.getIMEI());
//                mTvFirmwareVersion.setText(m20CurrentStateInfo.getSw_version());
//                mTvBoardType.setText(m20CurrentStateInfo.getGpsCard());
//                mTvInstallLocation.setText(m20CurrentStateInfo.getLocation());
//                mTvStorageState.setText(m20CurrentStateInfo.getEMMCFree());
//                mTvContinuousRunningTime.setText("--");
//
//                mTvDeviceStarNum.setText(m20CurrentStateInfo.getStarNum());
//                mTvPhoneStarNum.setText("--");
//                mTvAmsConnectionStatus.setText("--");
//                mTv4gSignalStrength.setText(String.format("%sdBm", m20CurrentStateInfo.get_$4g_signal()));
//                mTvLinkOneStatus.setText("未开启");
//                mTvLinkOneStatus.setTextColor(GlobalUtil.getColor(R.color.device_unopened_platform));
//                mTvLinkTwoStatus.setText("未开启");
//                mTvLinkTwoStatus.setTextColor(GlobalUtil.getColor(R.color.device_unopened_platform));
//
//                initLinkStatus(mTvLinkThreeStatus, m20CurrentStateInfo.getDataCenter3());
//                initLinkStatus(mTvLinkFourStatus, m20CurrentStateInfo.getDataCenter4());
//
//                boolean sensorAbnormal = false;
//                for(SensorErrnoBean errnoBean :m20CurrentStateInfo.getSensor_errno()){
//                    if (errnoBean.getErrno() != 0) {
//                        sensorAbnormal = true;
//                    }
//                }
//                mTvSensorStatus.setText(sensorAbnormal ? "异常" : "正常");
//                mTvSensorStatus.setTextColor(sensorAbnormal ? GlobalUtil.getColor(R.color.red) : GlobalUtil.getColor(R.color.text_color_3AD094));
//
//                mTvInclination.setText(m20CurrentStateInfo.getZ_Angle());
//                mTvInternalVoltage.setText(String.format("%s V", m20CurrentStateInfo.getInner_power_volt()));
//                mTvExternalVoltage.setText(String.format("%s V", m20CurrentStateInfo.getExt_power_volt()));
//                mTvSolarPanelVoltage.setText(String.format("%s V", m20CurrentStateInfo.getSolar_volt()));
//                mTvAmbientTemprature.setText(String.format("%s ℃", m20CurrentStateInfo.getTemp()));
//                mTvAmbientHumidity.setText(String.format("%s %%", m20CurrentStateInfo.getHumidity()));
//                mTvSupplementaryPower.setText(String.format("%s V", m20CurrentStateInfo.getSupply_power()));
//                mTvPowerConsumption.setText(String.format("%s V", m20CurrentStateInfo.getConsume_power()));
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 修改中心状态
     *
     * @param
     * @param linkStatus
     */
    private void initLinkStatus(TextView tvLinkStatus, String linkStatus) {
        if (linkStatus.equals("1")) {
            tvLinkStatus.setText("已连接");
            tvLinkStatus.setTextColor(GlobalUtil.getColor(R.color.title_text_color));
        } else if (linkStatus.equals("0")) {
            tvLinkStatus.setText("未连接");
            tvLinkStatus.setTextColor(GlobalUtil.getColor(R.color.device_not_connected_platform));
        }
    }
}
