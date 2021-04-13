package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.model.VersionMessageInfo;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 通过网络下发指令查看设备当前运行状态
 */
public class NetDasCurrentStateFragment extends BaseNetIotCommunicateFragment {

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
     * Das倾角计
     */
    @BindView(R.id.dasInclinometerInfo)
    View inclinometerLayout;

    @BindView(R.id.tv_inclinometer_status)
    TextView mTvInclinometerStatus;

    @BindView(R.id.tv_inclinometer_x_axis)
    TextView mTvInclinometerXaxis;

    @BindView(R.id.tv_inclinometer_y_axis)
    TextView mTvInclinometerYaxis;

    @BindView(R.id.tv_inclinometer_z_axis)
    TextView mTvInclinometerZaxis;

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
    @BindView(R.id.tv_switch_type)
    TextView mTvSwitchType;// 开关量值

    @BindView(R.id.ll_rain)
    View rainLayout;

    @BindView(R.id.tv_rain_value)
    TextView mTvRain;//雨量值

    @BindView(R.id.ll_wire_break_alarm)
    View wireBreakAlarmLayout;

    @BindView(R.id.tv_alarm_status)
    TextView mTvAlarmStatus;//断线报警器状态

    @BindView(R.id.sensor_recyclerView)
    RecyclerView sensorRecyclerView;

    private String collectorModel = "";//采集器类型

    private List<String> sensorList = new ArrayList<>();
    private CommonAdapter sensorAdapter;

    private VersionMessageInfo versionMessageInfo;


    public static NetDasCurrentStateFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetDasCurrentStateFragment fragment = new NetDasCurrentStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.das_current_state_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRefreshLayout();
        initAdapter();
        // 进入页面，刷新数据
        swipeRefresh.setRefreshing(true);
        queryStatusOne();
    }

    private void initRefreshLayout() {
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryStatusOne();
            }
        });
    }

    private void initAdapter() {
        sensorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sensorRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, DensityUtil.Dp2Px(mActivity, 10f), getResources().getColor(R.color.transparent)));
        sensorAdapter = new CommonAdapter<String>(getActivity(), R.layout.item_sensor_status, sensorList) {
            @Override
            protected void convert(CommonViewHolder holder, String string, int position) {
                //①:②:③，其中①：传感器地址，②：传感器状态，0正常，1异常，③：传感器数据
                String[] result = string.split(":");
                holder.setText(R.id.tv_address, "通道" + result[0]);

                if (result[1].equals("0")) {
                    holder.setText(R.id.tv_status, "正常");
                    holder.setTextColorRes(R.id.tv_status, R.color.text_color_3AD094);
                } else if (result[1].equals("1")) {
                    holder.setText(R.id.tv_status, "异常");
                    holder.setTextColorRes(R.id.tv_status, R.color.red);
                }

                if (collectorModel.equals("3")) {
                    holder.setText(R.id.tv_value, result[2] + "%rh");
                } else if (collectorModel.equals("21")) {
                    holder.setText(R.id.tv_value, result[2] + "Hz");
                } else {
                    holder.setText(R.id.tv_value, result[2] + "mm");
                }
            }
        };

        sensorRecyclerView.setAdapter(sensorAdapter);
    }

    private void queryStatusOne() {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DAS_STATUS_1);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
        Timber.i("查询设备状态1：%s", command);
    }

    /**
     * 获取设备的当前状态
     */
    private void queryStateInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            swipeRefresh.setRefreshing(false);
            ToastUtils.show("下发指令失败");
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(0);
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
        swipeRefresh.setRefreshing(false);
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
        swipeRefresh.setRefreshing(false);
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
        swipeRefresh.setRefreshing(false);
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
                content = content.replace("\\", "");
                content = content.replace("000_1:", "");
                try {
//                    devcieCurrentState = GsonFactory.getGson().fromJson(content, DevcieCurrentState.class);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                initStatusInfo();
            }
            break;

            default:
                break;
        }
    }

    private void initStatusInfo() {
//        if (devcieCurrentState != null) {
//            mTvDeviceSn.setText(projectDeviceInfo.getToken());
//            mTVSimCardNumber.setText("--");
//            if (projectDeviceInfo.getDeviceSimList() != null && projectDeviceInfo.getDeviceSimList().size() != 0) {
//                ProjectDeviceInfo.DeviceSimListBean simListBean = projectDeviceInfo.getDeviceSimList().get(0);
//                if (simListBean != null) {
//                    mTVSimCardNumber.setText(simListBean.getCcid());
//                }
//            }
//            mTvImeiNumber.setText(devcieCurrentState.getIMEI());
//            mTvDeviceStartCode.setText("--");
//            mTvFirmwareVersion.setText(devcieCurrentState.getSw_version());
//            mTvInstallPosition.setText(devcieCurrentState.getLocation());
//
//            mTvSignalStrength.setText("--");
//            if (projectDeviceInfo.getDeviceSimList() != null && projectDeviceInfo.getDeviceSimList().size() != 0) {
//                ProjectDeviceInfo.DeviceSimListBean simListBean = projectDeviceInfo.getDeviceSimList().get(0);
//                if (simListBean != null) {
//                    mTvSignalStrength.setText(simListBean.getSimIsp());
//                }
//            }
//
//            //数据中心
//            mTvSignalStrength.setCompoundDrawablesWithIntrinsicBounds(0, 0, DeviceCurrentRunStateUtils.getSignalResIdByRSSIValue(devcieCurrentState.get_$4g_signal()), 0);
//            mTvLinkOneStatus.setText("--");
//            mTvLinkOneSendData.setText("--");
//            mTvLinkOneUnsendData.setText("--");
//            mTvLinkOneOnlineRate.setText("--");
//
//            mTvLinkTwoStatus.setText("--");
//            mTvLinkTwoSendData.setText("--");
//            mTvLinkTwoUnsendData.setText("--");
//            mTvLinkTwoOnlineRate.setText("--");
//
//            mTvLinkThreeStatus.setText("--");
//            mTvLinkThreeSendData.setText("--");
//            mTvLinkThreeoUnsendData.setText("--");
//            mTvLinkThreeOnlineRate.setText("--");
//
//            //太阳能控制器
//            mTvSolarStatus.setText("--");
//            mTvSolarVoltage.setText(String.format("%sV", devcieCurrentState.getSolar_volt()));
//            mTvBatteryVoltage.setText(String.format("%sV", devcieCurrentState.getBattery_volt()));
//            mTvSupplyPower.setText(String.format("%sW", devcieCurrentState.getSupply_power()));
//            mTvConsumePower.setText(String.format("%sW", devcieCurrentState.getConsume_power()));
//
//            //机箱内部温湿度
//            mTvInternalStatus.setText("--");
//            mTvInternalTemperature.setText(String.format("%s°", devcieCurrentState.getTemp()));
//            mTvInternalHumidity.setText(devcieCurrentState.getHumidity() + "%");
//
//            //机箱外部温湿度
//            mTvExternalStatus.setText("--");
//            mTvExternalTemperature.setText(String.format("%s°", devcieCurrentState.getTemp_out()));
//            mTvExternalHumidity.setText(devcieCurrentState.getHumidity_out() + "%");
//
//            //设备电压
//            String powerStr = DeviceCurrentRunStateUtils.setDeviceInternalBattery(devcieCurrentState.getInner_power_volt());
//            double power = Double.parseDouble(powerStr.replace("%", ""));
//            SpannableStringBuilder builder = new SpannableStringBuilder(powerStr);
//            ForegroundColorSpan colorSpan = new ForegroundColorSpan(power <= 10 ? getContext().getResources().getColor(R.color.red) : getContext().getResources().getColor(R.color.text_color_3AD094));
//            builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            mTvDeviceInternalPower.setText(builder);
//
//            double voltage = devcieCurrentState.getExt_power_volt();
//            builder = new SpannableStringBuilder(voltage + "V");
//            colorSpan = new ForegroundColorSpan(voltage <= 5 ? getContext().getResources().getColor(R.color.red) : getContext().getResources().getColor(R.color.text_color_3AD094));
//            builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            mTvDeviceExternalVoltage.setText(builder);
//        }
    }
}
