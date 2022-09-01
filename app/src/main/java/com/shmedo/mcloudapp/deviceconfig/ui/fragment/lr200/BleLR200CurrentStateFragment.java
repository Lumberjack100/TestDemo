package com.shmedo.mcloudapp.deviceconfig.ui.fragment.lr200;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.GsonUtils;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.MonitoringType;
import com.shmedo.configlibrary.iot.enums.SensorErrorType;
import com.shmedo.configlibrary.iot.model.m20.M20CurrentStateInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/25 <br/>
 * 描述：      LR200蓝牙模式 设备运行状态页面
 */
public class BleLR200CurrentStateFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

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

    @BindView(R.id.tv_install_location)
    TextView mTvInstallLocation;

    @BindView(R.id.tv_internal_voltage)
    TextView mTvInternalVoltage;//内部电压

    @BindView(R.id.tv_external_voltage)
    TextView mTvExternalVoltage;//外部电压

    @BindView(R.id.tv_internal_temperature)
    TextView mTvInternalTemperature;

    @BindView(R.id.tv_internal_humidity)
    TextView mTvInternalHumidity;

    @BindView(R.id.tv_external_temperature)
    TextView mTvExternalTemperature;

    @BindView(R.id.tv_external_humidity)
    TextView mTvExternalHumidity;

    /**
     * 通讯状态
     */
    @BindView(R.id.tv_4g_signal_strength)
    TextView mTv4gSignalStrength;//4G信号强度

    @BindView(R.id.tv_link_one_status)
    TextView mTvLinkOneStatus;

    @BindView(R.id.tv_link_two_status)
    TextView mTvLinkTwoStatus;

    @BindView(R.id.tv_link_three_status)
    TextView mTvLinkThreeStatus;

    @BindView(R.id.tv_link_four_status)
    TextView mTvLinkFourStatus;

    /**
     * 裂缝计
     */
    @BindView(R.id.dasInclinometerInfo)
    View inclinometerLayout;

    @BindView(R.id.tv_mems_title)
    TextView mTvMemsTitle;

    @BindView(R.id.tv_inclinometer_status)
    TextView mTvInclinometerStatus;

    @BindView(R.id.ll_inclinometer_axis)
    View inclinometerAxisLayout;

    @BindView(R.id.tv_axis_x)
    TextView mTvAxisX;//角度

    @BindView(R.id.tv_axis_y)
    TextView mTvAxisY;//角度

    @BindView(R.id.tv_axis_z)
    TextView mTvAxisZ;//角度


    private M20CurrentStateInfo m20CurrentStateInfo;

    public static BleLR200CurrentStateFragment newInstance() {
        return new BleLR200CurrentStateFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.lr200_current_state_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.refresh_failed_while_device_disconnected));
                    mRefreshLayout.finishRefresh(false);
                    return;
                }
                queryStateInfo();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_10000_MILLIS);
            }
        });
    }

    /**
     * 获取设备的当前状态
     */
    private void queryStateInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS: {
                mRefreshLayout.finishRefresh(true);
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
            m20CurrentStateInfo = GsonUtils.fromJson(content, M20CurrentStateInfo.class);
            if (m20CurrentStateInfo != null) {
                mTvDeviceSn.setText(m20CurrentStateInfo.getSN());
                mTVSimCardNumber.setText(m20CurrentStateInfo.getCCID());
                mTvImeiNumber.setText(m20CurrentStateInfo.getIMEI());
                mTvFirmwareVersion.setText(m20CurrentStateInfo.getSw_version());
                mTvInstallLocation.setText(m20CurrentStateInfo.getLocation());
                mTvInternalVoltage.setText(String.format("%sV", m20CurrentStateInfo.getInner_power_volt()));
                mTvExternalVoltage.setText(String.format("%sV", m20CurrentStateInfo.getExt_power_volt()));
                mTvInternalTemperature.setText(String.format("%s℃", m20CurrentStateInfo.getTemp()));
                mTvInternalHumidity.setText(String.format("%s%%", m20CurrentStateInfo.getHumidity()));
                mTvExternalTemperature.setText(String.format("%s℃", m20CurrentStateInfo.getTemp_out()));
                mTvExternalHumidity.setText(String.format("%s%%", m20CurrentStateInfo.getHumidity_out()));

                mTv4gSignalStrength.setText(String.format("%sdBm", m20CurrentStateInfo.get_$4g_signal()));
                initLinkStatus(mTvLinkOneStatus, m20CurrentStateInfo.getDataCenter1());
                initLinkStatus(mTvLinkTwoStatus, m20CurrentStateInfo.getDataCenter2());
                initLinkStatus(mTvLinkThreeStatus, m20CurrentStateInfo.getDataCenter3());
                initLinkStatus(mTvLinkFourStatus, m20CurrentStateInfo.getDataCenter4());

                if (!m20CurrentStateInfo.getSensor_errno().isEmpty()) {
                    inclinometerLayout.setVisibility(View.VISIBLE);
                    int errno = m20CurrentStateInfo.getSensor_errno().get(0).getErrno();
                    String sensor_id = m20CurrentStateInfo.getSensor_errno().get(0).getSensor_id();
                    MonitoringType monitoringType = MonitoringType.valueByCode(sensor_id);
                    mTvMemsTitle.setText(monitoringType.getDescription());
                    mTvInclinometerStatus.setText(SensorErrorType.getErrorMessageByCode(String.valueOf(errno)));
                    setSensorStatusColor(mTvInclinometerStatus, errno);

                    if (!TextUtils.isEmpty(m20CurrentStateInfo.getX_Angle()) && !TextUtils.isEmpty(m20CurrentStateInfo.getY_Angle()) && !TextUtils.isEmpty(m20CurrentStateInfo.getZ_Angle())) {
                        inclinometerAxisLayout.setVisibility(View.VISIBLE);
                        mTvAxisX.setText(MessageFormat.format("{0}", m20CurrentStateInfo.getX_Angle()));
                        mTvAxisY.setText(MessageFormat.format("{0}", m20CurrentStateInfo.getY_Angle()));
                        mTvAxisZ.setText(MessageFormat.format("{0}", m20CurrentStateInfo.getZ_Angle()));
                    }
                }
            }
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
        if (linkStatus.equals("0")) {
            tvLinkStatus.setText("未开启");
            tvLinkStatus.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.device_unopened_platform));
        } else if (linkStatus.equals("1")) {
            tvLinkStatus.setText("已上线");
            tvLinkStatus.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
        } else if (linkStatus.equals("2")) {
            tvLinkStatus.setText("未上线");
            tvLinkStatus.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.device_not_connected_platform));
        }
    }

    private void setSensorStatusColor(TextView textView, int status) {
        if (status == 0) {
            textView.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
        } else {
            textView.setTextColor(Color.RED);
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_SMART_REFRESH:
                if (mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.finishRefresh(false);
                    ToastUtils.show("刷新超时");
                }
                break;
        }
    }
}
