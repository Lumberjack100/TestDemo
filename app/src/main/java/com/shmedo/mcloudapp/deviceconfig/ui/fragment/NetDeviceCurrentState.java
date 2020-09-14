package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.util.DeviceCurrentRunStateUtils;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import butterknife.BindView;

/**
 * 通过网络下发指令查看设备当前运行状态
 */
public class NetDeviceCurrentState extends BaseFragment {
    private static final String DEVICE_INFO = "device_info";
    private static final String DEVICE_CURRENT_STATE = "device_current_state";

    /**
     * 基本信息
     */
    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_sim_card_number)
    TextView mTVSimCardNumber;

    @BindView(R.id.tv_imei_number)
    TextView mTvImeiNumber;

    @BindView(R.id.tv_device_start_code)
    TextView mTvDeviceStartCode;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    @BindView(R.id.tv_install_position)
    TextView mTvInstallPosition;

    /**
     * 数据中心
     */
    @BindView(R.id.tv_signal_strength)
    TextView mTvSignalStrength;

    @BindView(R.id.tv_link_one_status)
    TextView mTvLinkOneStatus;

    @BindView(R.id.tv_link_one_send_data)
    TextView mTvLinkOneSendData;

    @BindView(R.id.tv_link_one_unsend_data)
    TextView mTvLinkOneUnsendData;

    @BindView(R.id.tv_link_one_online_rate)
    TextView mTvLinkOneOnlineRate;


    @BindView(R.id.tv_link_two_status)
    TextView mTvLinkTwoStatus;

    @BindView(R.id.tv_link_two_send_data)
    TextView mTvLinkTwoSendData;

    @BindView(R.id.tv_link_two_unsend_data)
    TextView mTvLinkTwoUnsendData;

    @BindView(R.id.tv_link_two_online_rate)
    TextView mTvLinkTwoOnlineRate;


    @BindView(R.id.tv_link_three_status)
    TextView mTvLinkThreeStatus;

    @BindView(R.id.tv_link_three_send_data)
    TextView mTvLinkThreeSendData;

    @BindView(R.id.tv_link_three_unsend_data)
    TextView mTvLinkThreeoUnsendData;

    @BindView(R.id.tv_link_three_online_rate)
    TextView mTvLinkThreeOnlineRate;


    /**
     * 太阳能控制器
     */
    @BindView(R.id.tv_solar_status)
    TextView mTvSolarStatus;

    @BindView(R.id.tv_solar_voltage)
    TextView mTvSolarVoltage;

    @BindView(R.id.tv_battery_voltage)
    TextView mTvBatteryVoltage;

    @BindView(R.id.tv_supply_power)
    TextView mTvSupplyPower;

    @BindView(R.id.tv_consume_power)
    TextView mTvConsumePower;

    /**
     * 机箱内部温湿度
     */
    @BindView(R.id.tv_internal_status)
    TextView mTvInternalStatus;

    @BindView(R.id.tv_internal_temperature)
    TextView mTvInternalTemperature;

    @BindView(R.id.tv_internal_humidity)
    TextView mTvInternalHumidity;

    /**
     * 机箱外部温湿度
     */
    @BindView(R.id.tv_external_status)
    TextView mTvExternalStatus;

    @BindView(R.id.tv_external_temperature)
    TextView mTvExternalTemperature;

    @BindView(R.id.tv_external_humidity)
    TextView mTvExternalHumidity;

    /**
     * 设备电压
     */
    @BindView(R.id.tv_device_internal_power)
    TextView mTvDeviceInternalPower;//设备内部电量

    @BindView(R.id.tv_device_external_voltage)
    TextView mTvDeviceExternalVoltage;//设备外部电压

    /**
     * 传感器
     */


    private DeviceCurrentStateActivity stateActivity;
    private ProjectDeviceInfo projectDeviceInfo;
    private DevcieCurrentState devcieCurrentState;


    public static NetDeviceCurrentState newInstance(ProjectDeviceInfo projectDeviceInfo, DevcieCurrentState devcieCurrentState) {
        NetDeviceCurrentState fragment = new NetDeviceCurrentState();
        Bundle args = new Bundle();
        args.putParcelable(DEVICE_INFO, projectDeviceInfo);
        args.putParcelable(DEVICE_CURRENT_STATE, devcieCurrentState);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(DEVICE_INFO);
            devcieCurrentState = getArguments().getParcelable(DEVICE_CURRENT_STATE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_device_current_state;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stateActivity = (DeviceCurrentStateActivity) mActivity;

        if (projectDeviceInfo != null && devcieCurrentState != null) {
            initBasicInfo();
            initDataServer();
            initSolarController();
            initInternalInfo();
            initExternalInfo();
            initDevicePowerAndVoltage();
        }
    }

    /**
     * 基本信息
     */
    private void initBasicInfo() {
        mTvDeviceSn.setText(TextUtils.isEmpty(projectDeviceInfo.getToken()) ? "--" : projectDeviceInfo.getToken());
        mTVSimCardNumber.setText("--");
        if (projectDeviceInfo.getDeviceSimList() != null && projectDeviceInfo.getDeviceSimList().size() != 0) {
            ProjectDeviceInfo.DeviceSimListBean simListBean = projectDeviceInfo.getDeviceSimList().get(0);
            if (simListBean != null) {
                mTVSimCardNumber.setText(TextUtils.isEmpty(simListBean.getCcid()) ? "--" : simListBean.getCcid());
            }
        }
        mTvImeiNumber.setText(TextUtils.isEmpty(devcieCurrentState.getIMEI()) ? "--" : devcieCurrentState.getIMEI());
        mTvDeviceStartCode.setText("--");
        mTvFirmwareVersion.setText(TextUtils.isEmpty(devcieCurrentState.getSw_version()) ? "--" : devcieCurrentState.getSw_version());
        mTvInstallPosition.setText(TextUtils.isEmpty(devcieCurrentState.getLocation()) ? "--" : devcieCurrentState.getLocation());
    }

    /**
     * 数据中心
     */
    private void initDataServer() {
        mTvSignalStrength.setText("--");
        if (projectDeviceInfo.getDeviceSimList() != null && projectDeviceInfo.getDeviceSimList().size() != 0) {
            ProjectDeviceInfo.DeviceSimListBean simListBean = projectDeviceInfo.getDeviceSimList().get(0);
            if (simListBean != null) {
                mTvSignalStrength.setText(TextUtils.isEmpty(simListBean.getSimIsp()) ? "--" : simListBean.getSimIsp());
            }
        }
        mTvSignalStrength.setCompoundDrawablesWithIntrinsicBounds(0, 0, DeviceCurrentRunStateUtils.getSignalResIdByRSSIValue(devcieCurrentState.get_$4g_signal()), 0);

        mTvLinkOneStatus.setText("--");
        mTvLinkOneSendData.setText("--");
        mTvLinkOneUnsendData.setText("--");
        mTvLinkOneOnlineRate.setText("--");

        mTvLinkTwoStatus.setText("--");
        mTvLinkTwoSendData.setText("--");
        mTvLinkTwoUnsendData.setText("--");
        mTvLinkTwoOnlineRate.setText("--");

        mTvLinkThreeStatus.setText("--");
        mTvLinkThreeSendData.setText("--");
        mTvLinkThreeoUnsendData.setText("--");
        mTvLinkThreeOnlineRate.setText("--");
    }

    /**
     * 太阳能控制器
     */
    private void initSolarController() {
        mTvSolarStatus.setText("--");
        mTvSolarVoltage.setText(String.format("%sV", devcieCurrentState.getSolar_volt()));
        mTvBatteryVoltage.setText(String.format("%sV", devcieCurrentState.getBattery_volt()));
        mTvSupplyPower.setText(String.format("%sW", devcieCurrentState.getSupply_power()));
        mTvConsumePower.setText(String.format("%sW", devcieCurrentState.getConsume_power()));
    }

    /**
     * 机箱内部温湿度
     */
    private void initInternalInfo() {
        mTvInternalStatus.setText("--");
        mTvInternalTemperature.setText(String.format("%s°", devcieCurrentState.getTemp()));
        mTvInternalHumidity.setText(devcieCurrentState.getHumidity() + "%");
    }

    /**
     * 机箱外部温湿度
     */
    private void initExternalInfo() {
        mTvExternalStatus.setText("--");
        mTvExternalTemperature.setText(String.format("%s°", devcieCurrentState.getTemp_out()));
        mTvExternalHumidity.setText(devcieCurrentState.getHumidity_out() + "%");
    }

    /**
     * 设备电压
     */
    private void initDevicePowerAndVoltage() {
        String powerStr = DeviceCurrentRunStateUtils.setDeviceInternalBattery(devcieCurrentState.getInner_power_volt());
        double power = Double.parseDouble(powerStr.replace("%", ""));
        SpannableStringBuilder builder = new SpannableStringBuilder(powerStr);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(power <= 10 ? getContext().getResources().getColor(R.color.red) : getContext().getResources().getColor(R.color.text_color_3AD094));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvDeviceInternalPower.setText(builder);

        double voltage = devcieCurrentState.getExt_power_volt();
        builder = new SpannableStringBuilder(voltage + "V");
        colorSpan = new ForegroundColorSpan(voltage <= 5 ? getContext().getResources().getColor(R.color.red) : getContext().getResources().getColor(R.color.text_color_3AD094));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvDeviceExternalVoltage.setText(builder);
    }


}
