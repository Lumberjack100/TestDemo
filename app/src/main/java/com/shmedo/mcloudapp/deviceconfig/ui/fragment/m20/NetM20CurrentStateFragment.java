package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.m20.M20CurrentStateInfo;
import com.shmedo.configlibrary.iot.model.m20.SensorErrnoBean;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/18/21 <br/>
 * 描述：    M20网络模式 设备运行状态页面
 */
public class NetM20CurrentStateFragment extends BaseNetIotCommunicateFragment {
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

    @BindView(R.id.tv_board_type)
    TextView mTvBoardType;//板卡类型

    @BindView(R.id.tv_install_location)
    TextView mTvInstallLocation;

    @BindView(R.id.tv_storage_state)
    TextView mTvStorageState;//存储状态

    @BindView(R.id.tv_continuous_running_time)
    TextView mTvContinuousRunningTime;//连续运行时间

    /**
     * 通讯状态
     */
    @BindView(R.id.tv_device_star_num)
    TextView mTvDeviceStarNum;//设备搜星数量

    @BindView(R.id.tv_phone_star_num)
    TextView mTvPhoneStarNum;//手机搜星数量

    @BindView(R.id.tv_ams_connection_status)
    TextView mTvAmsConnectionStatus;//AMS连接状态

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
     * 设备工作信息
     */
    @BindView(R.id.tv_sensor_status)
    TextView mTvSensorStatus;//传感器状态

    @BindView(R.id.tv_inclination)
    TextView mTvInclination;//倾角

    @BindView(R.id.tv_internal_voltage)
    TextView mTvInternalVoltage;//内部电压

    @BindView(R.id.tv_external_voltage)
    TextView mTvExternalVoltage;//外部电压

    @BindView(R.id.tv_solar_panel_voltage)
    TextView mTvSolarPanelVoltage;//太阳能板电压

    @BindView(R.id.tv_ambient_temperature)
    TextView mTvAmbientTemprature;//环境温度

    @BindView(R.id.tv_ambient_humidity)
    TextView mTvAmbientHumidity;//环境湿度

    @BindView(R.id.tv_supplementary_power)
    TextView mTvSupplementaryPower;//近12小时补充功率

    @BindView(R.id.tv_power_consumption)
    TextView mTvPowerConsumption;//消耗功率

    private M20CurrentStateInfo m20CurrentStateInfo;

    public static NetM20CurrentStateFragment newInstance(DeviceInfo deviceInfo) {
        NetM20CurrentStateFragment fragment = new NetM20CurrentStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.m20_current_state_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                queryStateInfo();
            }
        });
    }

    /**
     * 获取设备的当前状态
     */
    private void queryStateInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            ToastUtils.show("下发指令失败");
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
        ToastUtils.show("查询设备状态响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
        ToastUtils.show("查询设备状态响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        mRefreshLayout.finishRefresh(true);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS: {
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
                mTvBoardType.setText(m20CurrentStateInfo.getGpsCard());
                mTvInstallLocation.setText(m20CurrentStateInfo.getLocation());
                mTvStorageState.setText(m20CurrentStateInfo.getEMMCFree());
                mTvContinuousRunningTime.setText("--");

                mTvDeviceStarNum.setText(m20CurrentStateInfo.getStarNum());
                mTvPhoneStarNum.setText("--");
                mTvAmsConnectionStatus.setText("--");
                mTv4gSignalStrength.setText(String.format("%sdBm", m20CurrentStateInfo.get_$4g_signal()));
                mTvLinkOneStatus.setText("未开启");
                mTvLinkOneStatus.setTextColor(ColorUtils.getColor(R.color.device_unopened_platform));
                mTvLinkTwoStatus.setText("未开启");
                mTvLinkTwoStatus.setTextColor(ColorUtils.getColor(R.color.device_unopened_platform));

                initLinkStatus(mTvLinkThreeStatus, m20CurrentStateInfo.getDataCenter3());
                initLinkStatus(mTvLinkFourStatus, m20CurrentStateInfo.getDataCenter4());

                boolean sensorAbnormal = false;
                for (SensorErrnoBean errnoBean : m20CurrentStateInfo.getSensor_errno()) {
                    if (errnoBean.getErrno() != 0) {
                        sensorAbnormal = true;
                    }
                }
                mTvSensorStatus.setText(sensorAbnormal ? "未接入" : "正常");
                mTvSensorStatus.setTextColor(sensorAbnormal ? ColorUtils.getColor(R.color.red) : ColorUtils.getColor(R.color.text_color_3AD094));

                String angel = String.format("%s°,%s°,%s°", m20CurrentStateInfo.getX_Angle(), m20CurrentStateInfo.getY_Angle(), m20CurrentStateInfo.getZ_Angle());
                mTvInclination.setText(angel);
                mTvInternalVoltage.setText(String.format("%sV", m20CurrentStateInfo.getInner_power_volt()));
                mTvExternalVoltage.setText(String.format("%sV", m20CurrentStateInfo.getExt_power_volt()));
                mTvSolarPanelVoltage.setText(String.format("%sV", m20CurrentStateInfo.getSolar_volt()));
                mTvAmbientTemprature.setText(String.format("%s℃", m20CurrentStateInfo.getTemp()));
                mTvAmbientHumidity.setText(String.format("%s%%", m20CurrentStateInfo.getHumidity()));
                mTvSupplementaryPower.setText(String.format("%sV", m20CurrentStateInfo.getSupply_power()));
                mTvPowerConsumption.setText(String.format("%sV", m20CurrentStateInfo.getConsume_power()));
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
        if (linkStatus.equals("1")) {
            tvLinkStatus.setText("已连接");
            tvLinkStatus.setTextColor(ColorUtils.getColor(R.color.title_text_color));
        } else if (linkStatus.equals("0")) {
            tvLinkStatus.setText("未连接");
            tvLinkStatus.setTextColor(ColorUtils.getColor(R.color.device_not_connected_platform));
        }
    }
}