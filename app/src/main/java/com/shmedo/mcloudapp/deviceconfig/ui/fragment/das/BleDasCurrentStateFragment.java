package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.InstallLocationEntity;
import com.shmedo.configlibrary.ble.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.ServerNumber;
import com.shmedo.configlibrary.ble.model.DeviceNetStatus;
import com.shmedo.configlibrary.ble.model.DeviceStatusInfoOne;
import com.shmedo.configlibrary.ble.model.DeviceStatusInfoThree;
import com.shmedo.configlibrary.ble.model.DeviceStatusInfoTwo;
import com.shmedo.configlibrary.ble.model.InclinometerInfo;
import com.shmedo.configlibrary.ble.model.SystemRunStateInfo;
import com.shmedo.configlibrary.ble.model.VersionMessageInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.util.DeviceCurrentRunStateUtils;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 测试通过下发指令查看设备当前运行状态
 */
public class BleDasCurrentStateFragment extends BaseBleCommunicateFragment {
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


    @Override
    protected int getLayoutId() {
        return R.layout.das_current_state_fragment;
    }

    public static BleDasCurrentStateFragment newInstance() {
        return new BleDasCurrentStateFragment();
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
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.refresh_failed_while_device_disconnected));
                    swipeRefresh.setRefreshing(false);
                    return;
                }
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
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable(null, CONFIG_PARAMS_DELAY_MILLIS);
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DAS_STATUS_1);
        sendCommand(command);
        Timber.i("查询设备状态1：%s", command);
    }

    @Override
    protected void doProgressRun() {
        super.doProgressRun();
        swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        if (!isActive) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        String command;
        String tempStr = cmdStr.replace(CommandResult.COMMAND_RESULT_HEADER, "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DAS_STATUS_1:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    swipeRefresh.setRefreshing(false);
                    stopProgressRunnable();
                    Timber.e("查询设备状态1指令出错!");
                    ToastUtils.show("查询设备状态1出错!");
                    return;
                }
                DeviceStatusInfoOne deviceStatusInfoOne = ResultParserUtil.getEntityObject(cmdStr);
                initDeviceStatusOne(deviceStatusInfoOne);

                command = CommandManager.getInstance().getCommand(CommandType.VERSION_MESSAGE);
                sendCommand(command);
                Timber.d("查询设备版本信息：%s", command);
                break;

            case VERSION_MESSAGE:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    swipeRefresh.setRefreshing(false);
                    stopProgressRunnable();
                    Timber.e("查询设备版本信息出错!");
                    ToastUtils.show("查询设备版本信息出错!");
                    return;
                }
                versionMessageInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (versionMessageInfo != null) {
                    mTvFirmwareVersion.setText(versionMessageInfo.getFirmwareVersion());
                }

                InstallLocationEntity installLocationEntity = new InstallLocationEntity(2);
                command = CommandManager.getInstance().getCommand(CommandType.INSTALL_LOCATION, installLocationEntity);
                sendCommand(command);
                Timber.i("查询安装位置：%s", command);
                break;

            case INSTALL_LOCATION:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    swipeRefresh.setRefreshing(false);
                    stopProgressRunnable();
                    Timber.e("查询安装位置出错!");
                    ToastUtils.show("查询安装位置出错!");
                    return;
                }
                String position = ResultParserUtil.getEntityObject(cmdStr);
                if (!TextUtils.isEmpty(position)) {
                    mTvInstallPosition.setText(position);
                }

                command = CommandManager.getInstance().getCommand(CommandType.SYSTEM_RUN_STATE);
                sendCommand(command);
                Timber.i("查询运行状态：%s", command);
                break;

            case SYSTEM_RUN_STATE:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    swipeRefresh.setRefreshing(false);
                    stopProgressRunnable();
                    Timber.e("查询运行状态出错!");
                    ToastUtils.show("查询运行状态出错!");
                    return;
                }
                SystemRunStateInfo runStateInfo = ResultParserUtil.getEntityObject(cmdStr);
                initOperatorInformation(runStateInfo);

                ServerNumberEntity addressNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt());
                command = CommandManager.getInstance().getCommand(CommandType.QUERY_NETWORK_STATUS, addressNumberEntity);
                sendCommand(command);
                Timber.i("查询数据中心网络状态：中心1==%s", command);
                break;

            case QUERY_NETWORK_STATUS://数据中心网络状态
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    swipeRefresh.setRefreshing(false);
                    stopProgressRunnable();
                    Timber.e("查询数据中心网络状态出错!");
                    ToastUtils.show("查询数据中心网络状态出错!");
                    return;
                }

                String number = tempStr.substring(3, 4);
                switch (number) {
                    case "1":
                        DeviceNetStatus internetStatus1 = ResultParserUtil.getEntityObject(cmdStr);
                        initDataCenterNetStatus(internetStatus1, number);
                        addressNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt());

                        command = CommandManager.getInstance().getCommand(CommandType.QUERY_NETWORK_STATUS, addressNumberEntity);
                        sendCommand(command);
                        Timber.i("查询网络状态：中心2==%s", command);
                        break;

                    case "2":
                        DeviceNetStatus internetStatus2 = ResultParserUtil.getEntityObject(cmdStr);
                        initDataCenterNetStatus(internetStatus2, number);
                        addressNumberEntity = new ServerNumberEntity(ServerNumber.NUMBER_THREE.toInt());

                        command = CommandManager.getInstance().getCommand(CommandType.QUERY_NETWORK_STATUS, addressNumberEntity);
                        sendCommand(command);
                        Timber.i("查询网络状态：中心3==%s", command);
                        break;

                    case "3":
                        DeviceNetStatus internetStatus3 = ResultParserUtil.getEntityObject(cmdStr);
                        initDataCenterNetStatus(internetStatus3, number);

                        command = CommandManager.getInstance().getCommand(CommandType.QUERY_DAS_STATUS_2);
                        sendCommand(command);
                        Timber.i("查询设备状态2：%s", command);
                        break;
                }
                break;

            case QUERY_DAS_STATUS_2:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    swipeRefresh.setRefreshing(false);
                    stopProgressRunnable();
                    Timber.e("查询设备状态2出错!");
                    ToastUtils.show("查询设备状态2出错!");
                    return;
                }
                DeviceStatusInfoTwo statusTwo = ResultParserUtil.getEntityObject(cmdStr);
                setDeviceStatusTwo(statusTwo);

                command = CommandManager.getInstance().getCommand(CommandType.QUERY_DAS_STATUS_3);
                sendCommand(command);
                Timber.i("查询设备状态3：%s", command);
                break;

            case QUERY_DAS_STATUS_3:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    swipeRefresh.setRefreshing(false);
                    stopProgressRunnable();
                    Timber.e("查询设备状态3出错!");
                    ToastUtils.show("查询设备状态3出错!");
                    return;
                }
                DeviceStatusInfoThree statusThree = ResultParserUtil.getEntityObject(cmdStr);
                if (statusThree != null) {
                    collectorModel = statusThree.getCollectorModel();
                    setDeviceStatusThree(statusThree);

                    //裂缝计采集器，查询倾角计信息
                    if (collectorModel.trim().equals("2")) {
                        processQueryInclinometerInfo();
                    }else{
                        swipeRefresh.setRefreshing(false);
                        stopProgressRunnable();
                    }
                }
                break;

            case QUERY_INCLINOMETER_INFO:
                swipeRefresh.setRefreshing(false);
                stopProgressRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询倾角计信息出错!");
                    return;
                }
                InclinometerInfo inclinometerInfo = ResultParserUtil.getEntityObject(cmdStr);
                setInclinometerInfo(inclinometerInfo);
                break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }


    /**
     * 设置设备状态1  ##041
     *
     * @param statusOne
     */
    private void initDeviceStatusOne(DeviceStatusInfoOne statusOne) {
        if (statusOne == null)
            return;

        mTvDeviceSn.setText(statusOne.getSnNumber());
        mTVSimCardNumber.setText(statusOne.getSimNumber());
        mTvImeiNumber.setText(statusOne.getImeiNumber());

        StringBuilder stringBuilder = new StringBuilder();
        String startCode1 = StringUtil.formatStringTwo(statusOne.getStartCodeOne());
        String startCode2 = StringUtil.formatStringTwo(statusOne.getStartCodeTwo());
        mTvDeviceStartCode.setText(stringBuilder.append(startCode1).append(startCode2).toString());
    }

    /**
     * 设置运营商信息  ##014
     *
     * @param runStateInfo
     */
    private void initOperatorInformation(SystemRunStateInfo runStateInfo) {
        if (runStateInfo == null)
            return;

        mTvSignalStrength.setCompoundDrawablesWithIntrinsicBounds(0, 0, DeviceCurrentRunStateUtils.getSignalResIdByCSQValue(Integer.parseInt(runStateInfo.getGprsSignal())), 0);
        mTvSignalStrength.setText(DeviceCurrentRunStateUtils.setOperatorType(runStateInfo.getOperator()));
    }

    /**
     * 设置网络状态  ##044
     */
    private void initDataCenterNetStatus(DeviceNetStatus internetStatus, String linkNumber) {
        if (internetStatus == null)
            return;

        switch (linkNumber) {
            case "1":
                initLinkStatus(mTvLinkOneStatus, mTvLinkOneSendData, mTvLinkOneUnsendData, internetStatus.getLinkStatus(),
                        internetStatus.getLinkEnable(), internetStatus.getSentData(), internetStatus.getGeneratedData());
                if (internetStatus.getOnlineRate() != null) {
                    mTvLinkOneOnlineRate.setText(internetStatus.getOnlineRate() + "%");
                }
                break;
            case "2":
                initLinkStatus(mTvLinkTwoStatus, mTvLinkTwoSendData, mTvLinkTwoUnsendData, internetStatus.getLinkStatus(),
                        internetStatus.getLinkEnable(), internetStatus.getSentData(), internetStatus.getGeneratedData());
                if (internetStatus.getOnlineRate() != null)
                    mTvLinkTwoOnlineRate.setText(internetStatus.getOnlineRate() + "%");
                break;
            case "3":
                initLinkStatus(mTvLinkThreeStatus, mTvLinkThreeSendData, mTvLinkThreeoUnsendData, internetStatus.getLinkStatus(),
                        internetStatus.getLinkEnable(), internetStatus.getSentData(), internetStatus.getGeneratedData());
                if (internetStatus.getOnlineRate() != null)
                    mTvLinkThreeOnlineRate.setText(internetStatus.getOnlineRate() + "%");
                break;
        }
    }

    /**
     * 修改中心状态
     *
     * @param
     * @param linkStatus
     */
    private void initLinkStatus(TextView tvLinkStatus, TextView tvSendData, TextView tvUnSendData, String linkStatus, String linkEnable, String sendData, String generatedData) {
        String unSendData = String.valueOf(Integer.parseInt(generatedData) - Integer.parseInt(sendData));

        //如果中心使能
        if (linkEnable.equals("1")) {
            if (linkStatus.equals("1")) {
                tvLinkStatus.setText("已上线");
                tvLinkStatus.setTextColor(GlobalUtil.getColor(R.color.text_color_3AD094));
            } else if (linkStatus.equals("0")) {
                tvLinkStatus.setText("未上线");
                tvLinkStatus.setTextColor(Color.RED);
            }
            tvSendData.setText(sendData);
            tvUnSendData.setText(unSendData);
        } else if (linkEnable.equals("0")) {//未使能
            tvLinkStatus.setText("未开启");
            tvLinkStatus.setTextColor(Color.GRAY);
            tvSendData.setText("0");
            tvUnSendData.setText("0");
        }
    }

    /**
     * 设置设备状态2  ##042
     *
     * @param statusTwo
     */
    private void setDeviceStatusTwo(DeviceStatusInfoTwo statusTwo) {
        if (statusTwo == null)
            return;

        //太阳能控制器
        setDeviceStatus(mTvSolarStatus, statusTwo.getSolarControllerStatus());
        mTvSolarVoltage.setText(statusTwo.getSolarPanelVoltage() + "V");
        mTvBatteryVoltage.setText(statusTwo.getBatteryVoltage() + "V");
        mTvSupplyPower.setText(statusTwo.getDailyPowerGeneration() + "W");
        mTvConsumePower.setText(statusTwo.getDailyPowerConsumption() + "W");

        //机箱内部温湿度
        setDeviceStatus(mTvInternalStatus, statusTwo.getInternalTempHumidityStatus());
        mTvInternalTemperature.setText(statusTwo.getInternalTemperature() + "°");
        mTvInternalHumidity.setText(statusTwo.getInternalHumidity() + "%");

        //机箱外部温湿度
        setDeviceStatus(mTvExternalStatus, statusTwo.getExternalTempHumidityStatus());
        mTvExternalTemperature.setText(statusTwo.getExternalTemperature() + "°");
        mTvExternalHumidity.setText(statusTwo.getExternalHumidity() + "%");
        //设备电量、电压
        processPowerAndVoltage(statusTwo);

        //雨量值 断线报警器状态
        //根据开关量类型来判断降雨量和断线报警器
        String type = statusTwo.getSwitchType();
        switch (type) {
            case "1":
                mTvSwitchType.setText("雨量计");
                rainLayout.setVisibility(View.VISIBLE);
                wireBreakAlarmLayout.setVisibility(View.GONE);
                mTvRain.setText(statusTwo.getRainfallStatus() + "mm");
                break;

            case "2":
                mTvSwitchType.setText("关闭");
                rainLayout.setVisibility(View.GONE);
                wireBreakAlarmLayout.setVisibility(View.GONE);
                break;

            case "3":
                mTvSwitchType.setText("断线报警器");
                rainLayout.setVisibility(View.GONE);
                wireBreakAlarmLayout.setVisibility(View.VISIBLE);
                if (statusTwo.getRainfallStatus().equals("1.0") || statusTwo.getRainfallStatus().equals("1")) {
                    mTvAlarmStatus.setText("已断线");
                    mTvAlarmStatus.setTextColor(Color.RED);
                } else if (statusTwo.getRainfallStatus().equals("0.0") || statusTwo.getRainfallStatus().equals("0")) {
                    mTvAlarmStatus.setText("未断线");
                    mTvAlarmStatus.setTextColor(getResources().getColor(R.color.text_color_3AD094));
                }
                break;
        }
    }

    private void setDeviceStatus(TextView textView, String status) {
        if (status.equals("1")) {
            textView.setText("异常");
            textView.setTextColor(Color.RED);
        } else if (status.equals("0")) {
            textView.setText("正常");
            textView.setTextColor(GlobalUtil.getColor(R.color.text_color_3AD094));
        }
    }

    /**
     * 设备电量、电压警戒值处理
     */
    private void processPowerAndVoltage(DeviceStatusInfoTwo statusTwo) {
        if (statusTwo == null)
            return;

        String powerStr = DeviceCurrentRunStateUtils.setDeviceInternalBattery(statusTwo.getInternalVoltage());
        double power = Double.parseDouble(powerStr.replace("%", ""));
        SpannableStringBuilder builder = new SpannableStringBuilder(powerStr);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(power <= 10 ? getContext().getResources().getColor(R.color.red) : getContext().getResources().getColor(R.color.text_color_3AD094));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvDeviceInternalPower.setText(builder);

        String voltageStr = statusTwo.getExternalVoltage();
        double voltage = Double.parseDouble(voltageStr);
        builder = new SpannableStringBuilder(voltageStr + "V");
        colorSpan = new ForegroundColorSpan(voltage <= 5 ? getContext().getResources().getColor(R.color.red) : getContext().getResources().getColor(R.color.text_color_3AD094));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvDeviceExternalVoltage.setText(builder);
    }

    /**
     * 设备状态3
     *
     * @param statusThree
     */
    private void setDeviceStatusThree(DeviceStatusInfoThree statusThree) {
        if (statusThree == null)
            return;

        if (statusThree.getCollectorAddress().equals("0")) {
            sensorRecyclerView.setVisibility(View.GONE);
        } else {
            sensorRecyclerView.setVisibility(View.VISIBLE);
            List<String> list = statusThree.getSensorStatus();
            sensorList.clear();
            sensorList.addAll(list);
            sensorAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 查询倾角计信息
     */
    private void processQueryInclinometerInfo() {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_INCLINOMETER_INFO);
        sendCommand(command);
        Timber.i("查询倾角计信息：%s", command);

//        if (TextUtils.isEmpty(versionMessageInfo.getFirmwareVersion()))
//            return;
//
//        String firmwareVersion = versionMessageInfo.getFirmwareVersion();
//        firmwareVersion = firmwareVersion.trim().toUpperCase().replace("V", "").replace(".", "");
//        if (ValidateUtil.isNumeric(firmwareVersion)) {
//            try {
//                int version = Integer.parseInt(firmwareVersion);
//                if (version >= 314) {
//                    String command = CommandManager.getInstance().getCommand(CommandType.QUERY_INCLINOMETER_INFO);
//                    sendCommand(command);
//                    Timber.i("查询倾角计信息：%s", command);
//                }
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
    }

    /**
     * 设置倾角计信息
     */
    private void setInclinometerInfo(InclinometerInfo inclinometerInfo) {
        if (inclinometerInfo == null) {
            inclinometerLayout.setVisibility(View.GONE);
            return;
        }

        inclinometerLayout.setVisibility(View.VISIBLE);
        setDeviceStatus(mTvInclinometerStatus, inclinometerInfo.getStatus());
        mTvInclinometerXaxis.setText(inclinometerInfo.getxAxis());
        mTvInclinometerYaxis.setText(inclinometerInfo.getyAxis());
        mTvInclinometerZaxis.setText(inclinometerInfo.getzAxis());
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}