package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.model.DeviceNetStatus;
import com.shmedo.configlibrary.ble.model.DeviceStatusInfoOne;
import com.shmedo.configlibrary.ble.model.DeviceStatusInfoThree;
import com.shmedo.configlibrary.ble.model.DeviceStatusInfoTwo;
import com.shmedo.configlibrary.ble.model.InclinometerInfo;
import com.shmedo.configlibrary.ble.model.SystemRunStateInfo;
import com.shmedo.configlibrary.ble.model.VersionMessageInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.util.DeviceCurrentRunStateUtils;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/9/18 <br/>
 * 描述：     TODO
 */
public class BleDasCurrentStateFragment extends BaseBleCommunicateFragment {
    @NonNull
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

    @BindView(R.id.tv_device_start_code)
    TextView mTvDeviceStartCode;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    @BindView(R.id.tv_install_position)
    TextView mTvInstallPosition;

    @BindView(R.id.tv_device_internal_power)
    TextView mTvDeviceInternalPower;//设备内部电量

    @BindView(R.id.tv_device_external_voltage)
    TextView mTvDeviceExternalVoltage;//设备外部电压

    /**
     * 数据中心
     */
    @BindView(R.id.tv_signal_strength)
    TextView mTvSignalStrength;

    @BindView(R.id.data_center_recyclerView)
    RecyclerView dataCenterRecyclerView;

    /**
     * 太阳能控制器
     */
    @BindView(R.id.solarInfoLayout)
    View solarInfoLayout;

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
    @BindView(R.id.internalTempHumidityLayout)
    View internalTempHumidityLayout;

    @BindView(R.id.tv_internal_status)
    TextView mTvInternalStatus;

    @BindView(R.id.tv_internal_temperature)
    TextView mTvInternalTemperature;

    @BindView(R.id.tv_internal_humidity)
    TextView mTvInternalHumidity;

    /**
     * 机箱外部温湿度
     */
    @BindView(R.id.externalTempHumidityLayout)
    View externalTempHumidityLayout;

    @BindView(R.id.tv_external_status)
    TextView mTvExternalStatus;

    @BindView(R.id.tv_external_temperature)
    TextView mTvExternalTemperature;

    @BindView(R.id.tv_external_humidity)
    TextView mTvExternalHumidity;

    /**
     * 辅传感器
     */
    // 开关量
    @BindView(R.id.dasIOSensorInfo)
    View ioSensorLayout;

    @BindView(R.id.tv_switch_status)
    TextView mTvSwitchStatus;

    @BindView(R.id.ll_rain)
    View rainLayout;

    @BindView(R.id.tv_rain_value)
    TextView mTvRain;//雨量值

    @BindView(R.id.ll_wire_break_alarm)
    View wireBreakAlarmLayout;

    @BindView(R.id.tv_alarm_status)
    TextView mTvAlarmStatus;//断线报警器状态

    //数字水位计
    @BindView(R.id.dasPiezometerInfo)
    View dasDigitalPiezometerLayout;

    @BindView(R.id.emptyPipeDistanceLayout)
    View emptyPipeDistanceLayout;

    @BindView(R.id.waterTemperatureLayout)
    View waterTemperatureLayout;

    @BindView(R.id.tv_piezometer_title)
    TextView mTvPiezometerTitle;

    @BindView(R.id.tv_piezometer_status)
    TextView mTvPiezometerStatus;

    @BindView(R.id.tv_piezometer_value)
    TextView mTvPiezometerValue;

    @BindView(R.id.tv_emptyPipeDistance)
    TextView mTvEmptyPipeDistance;

    @BindView(R.id.tv_waterTemperature)
    TextView mTvWaterTemperature;

    //Das倾角计
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

    @BindView(R.id.ll_inclinometer_acceleration)
    View inclinometerAccelerationLayout;

    @BindView(R.id.tv_acceleration_x)
    TextView mTvAccelerationX;//

    @BindView(R.id.tv_acceleration_y)
    TextView mTvAccelerationY;//

    @BindView(R.id.tv_acceleration_z)
    TextView mTvAccelerationZ;//

    //扩展传感器信息
    @BindView(R.id.mainSensorInfo)
    View mainSensorInfoLayout;

    @BindView(R.id.tv_mainSensor)
    TextView mTvMainSensor;

    @BindView(R.id.sensor_recyclerView)
    RecyclerView sensorRecyclerView;

    private String collectorModelStr = "";//采集器类型

    private List<DeviceNetStatus> dataCenterStatusInfoList = new ArrayList<>();
    private List<String> sensorList = new ArrayList<>();
    private CommonAdapter dataCenterAdapter, sensorAdapter;

    private VersionMessageInfo versionMessageInfo;
    private DecimalFormat decimalFormat = new DecimalFormat();

    @Override
    protected int getLayoutId() {
        return R.layout.das_current_state_fragment;
    }

    public static BleDasCurrentStateFragment newInstance() {
        return new BleDasCurrentStateFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDataCenterAdapter();
        initSensorAdapter();
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
                    ToastUtils.show(StringUtils.getString(R.string.refresh_failed_while_device_disconnected));
                    mRefreshLayout.finishRefresh(false);
                    return;
                }
                queryData();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_20000_MILLIS);
            }
        });
    }

    private void initDataCenterAdapter() {
        dataCenterRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dataCenterRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(10f), com.blankj.utilcode.util.ColorUtils.getColor(R.color.transparent)));
        dataCenterAdapter = new CommonAdapter<DeviceNetStatus>(getActivity(), R.layout.item_das_data_center_status, dataCenterStatusInfoList) {
            @Override
            protected void convert(CommonViewHolder holder, DeviceNetStatus netStatusInfo, int position) {
                holder.setText(R.id.tv_center_title, "中心" + netStatusInfo.getLinkNumber());
                holder.setText(R.id.tv_link_send_data, String.valueOf(netStatusInfo.getSentData()));
                String unSendData = String.valueOf(Integer.parseInt(netStatusInfo.getGeneratedData()) - Integer.parseInt(netStatusInfo.getSentData()));
                holder.setText(R.id.tv_link_unsend_data, unSendData);
                holder.setText(R.id.tv_link_online_rate, netStatusInfo.getOnlineRate() + "%");

                //未开启
                if (netStatusInfo.getLinkEnable().equals("0")) {
                    holder.setText(R.id.tv_link_status, "未开启");
                    holder.setTextColor(R.id.tv_link_status, Color.GRAY);
                    holder.setText(R.id.tv_link_send_data, "0");
                    holder.setText(R.id.tv_link_unsend_data, "0");
                    holder.setText(R.id.tv_link_online_rate, "0");

                } else {
                    if (netStatusInfo.getLinkStatus().equals("0")) {
                        holder.setText(R.id.tv_link_status, "未上线");
                        holder.setTextColor(R.id.tv_link_status, Color.RED);
                    } else if (netStatusInfo.getLinkStatus().equals("1")) {
                        holder.setText(R.id.tv_link_status, "已上线");
                        holder.setTextColorRes(R.id.tv_link_status, R.color.text_color_3AD094);
                    }
                }
            }
        };
        dataCenterRecyclerView.setAdapter(dataCenterAdapter);
    }

    /**
     * 扩展传感器信息
     */
    private void initSensorAdapter() {
        sensorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sensorRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(10f), com.blankj.utilcode.util.ColorUtils.getColor(R.color.transparent)));
        sensorAdapter = new CommonAdapter<String>(getActivity(), R.layout.item_sensor_status, sensorList) {
            @Override
            protected void convert(CommonViewHolder holder, String string, int position) {
                try {
                    IOTSensorType sensorType = IOTSensorType.value(collectorModelStr);
                    holder.setText(R.id.tv_sensor_name, sensorType.getDescription());
                    //①:②:③，其中①：传感器地址，②：传感器状态，0正常，1异常，③：传感器数据
                    String[] result = string.split(":");

                    String sensorAisle = sensorType == IOTSensorType.VW08 ? String.valueOf(Integer.parseInt(result[0]) + 1) : result[0];
                    holder.setText(R.id.tv_address, "通道" + sensorAisle);
                    if (result[1].equals("0")) {
                        holder.setText(R.id.tv_status, "正常");
                        holder.setTextColorRes(R.id.tv_status, R.color.text_color_3AD094);
                    } else if (result[1].equals("1")) {
                        holder.setText(R.id.tv_status, "未接入");
                        holder.setTextColorRes(R.id.tv_status, R.color.red);
                    }

                    if (collectorModelStr.equals("3")) {
                        holder.setText(R.id.tv_value1, result[2] + "%RH");
                    } else if (collectorModelStr.equals("21")) {
                        holder.setText(R.id.tv_value1, result[2] + "Hz");
                    } else {
                        holder.setText(R.id.tv_value1, result[2] + "mm");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        sensorRecyclerView.setAdapter(sensorAdapter);
    }

    private void queryData() {
        commandItems.clear();
        queryStatusOne();
        queryVersion();
        querySignalStrength();
        queryDataCenter(new ServerNumberEntity(ServerNumber.NUMBER_ONE.toInt()));
        queryDataCenter(new ServerNumberEntity(ServerNumber.NUMBER_TWO.toInt()));
        queryDataCenter(new ServerNumberEntity(ServerNumber.NUMBER_THREE.toInt()));
        queryStatusTwo();
        querySensorStatus();
        queryInclinometerInfo();

        sendCommandFromCmdList(null);
    }

    /**
     * 查询设备状态1: ##041\r\n <br/>
     * 应答: $$041,(1),(2),(3),(4),(5),(6)\r\n <br/>
     * （1）SN号 <br/>
     * （2）IMEI号 <br/>
     * （3）SIM卡号 <br/>
     * （4）启动代码1 <br/>
     * （5）启动代码2 <br/>
     * （6）信号强度，1~11为1格信号，12~18为2格信号，19~25为3格信号，26~31为4格信号 <br/>
     */
    private void queryStatusOne() {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DAS_STATUS_1);
        Timber.d("查询设备基本信息：%s", command);
        commandItems.add(command);
    }

    /**
     * 获取版本信息 ##040\r\n <br/>
     * 应答: $$040,(1),(2),(3)\r\n<br/>
     * (1)“产品序列号”<br/>
     * (2)“固件版本号”<br/>
     * (3)“生产日期”<br/>
     */
    private void queryVersion() {
        String command = CommandManager.getInstance().getCommand(CommandType.VERSION_MESSAGE);
        Timber.d("查询设备版本信息：%s", command);
        commandItems.add(command);
    }

    /**
     * 获取信号强度 ##014\r\n<br/>
     * 应答:$$014,(1),(2) ,(3),(4) ,(5),(6) ,(7),(8), (9),(10) \r\n<br/>
     * (1)：信号值<br/>
     * (2)：GPS定位搜星数目<br/>
     * (3)：启动代码<br/>
     * (4)：重启代码<br/>
     * (5)：sim卡ccid<br/>
     * (6)：设备内部温度<br/>
     * (7)：设备内部电池电压<br/>
     * (8)：设备外部电压<br/>
     * (9)：运营商类型<br/>
     * (10)：网络制式<br/>
     */
    private void querySignalStrength() {
        String command = CommandManager.getInstance().getCommand(CommandType.SYSTEM_RUN_STATE);
        Timber.d("获取信号强度：%s", command);
        commandItems.add(command);
    }

    /**
     * 查询数据中心网络状态 ##044n\r\n<br/>
     * 应答:$$044n,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)\r\n<br/>
     * 注：n取值1，2，3<br/>
     * （1）已发送数据<br/>
     * （2）已生成数据<br/>
     * （3）flash使能，取值0,1，1表示使能，0表示未使能<br/>
     * （4）flash读指针<br/>
     * （5）flash写指针<br/>
     * （6）链路使能：取值0,1，1表示使能，0表示未使能<br/>
     * （7）链路状态：取值0,1，1表示已上线，0表示未上线<br/>
     * （8）4G模块状态：<br/>
     * （9）MQTT状态：	<br/>
     * （10）在线率，单位%<br/>
     * 示例：$$0441,7,7,1,0x001000D4,0x001000D4,1,1,4,7,93.6
     */
    private void queryDataCenter(ServerNumberEntity addressNumberEntity) {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_NETWORK_STATUS, addressNumberEntity);
        Timber.d("查询数据中心网络状态：%s", command);
        commandItems.add(command);
    }

    /**
     * 查询设备状态2:##042\r\n<br/>
     * $$042,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(15),(16),(17),(18)\r\n<br/>
     * （1）SN号<br/>
     * （2）经度<br/>
     * （3）纬度<br/>
     * （4）设备内部电压<br/>
     * （5）设备外部电压<br/>
     * （6）太阳能控制器状态<br/>
     * （7）太阳能板电压<br/>
     * （8）电池电压<br/>
     * （9）日发电量<br/>
     * （10）日耗电量<br/>
     * （11）机箱内部温湿度状态<br/>
     * （12）机箱内部温度<br/>
     * （13）机箱内部湿度<br/>
     * （14）机箱外部温湿度状态<br/>
     * （15）机箱外部温度<br/>
     * （16）机箱外部湿度<br/>
     * （17）开关量类型，1：雨量计，2：关闭，3：断线报警器<br/>
     * （18）降雨量或断线报警器状态(1:断开，0：闭合)<br/>
     * 示例：$$042,150000L,0.000000,0.000000,8.4,11.9,0,1.1,11.9,0.0,0.0,0,23.1,35.9,0,23.8,35.1,2,0.0<br/>
     */
    private void queryStatusTwo() {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DAS_STATUS_2);
        Timber.d("查询设备状态2：%s", command);
        commandItems.add(command);
    }

    /**
     * 获取主传感器状态:##043\r\n<br/>
     * 应答:$$043,(1),(2),(3),(4),(5)<br/>
     * （1）SN号<br/>
     * （2）采集器型号<br/>
     * （3）采集器地址(当采集器地址为0时，关闭采集功能)<br/>
     * （4）传感器状态，用冒号分隔的字符串<br/>
     * ①:②:③，其中 ①：传感器地址，②：传感器状态，0正常，1异常，③：传感器数据<br/>
     * （5）传感器状态，和（2）格式相同，<br/>
     * 注：传感器状态可能有很多个，有接入传感器个数决定。<br/>
     */
    private void querySensorStatus() {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_DAS_STATUS_3);
        Timber.d("查询扩展传感器：%s", command);
        commandItems.add(command);
    }

    /**
     * 查询倾角计信息
     */
    private void queryInclinometerInfo() {
        String command = CommandManager.getInstance().getCommand(CommandType.QUERY_INCLINOMETER_INFO);
        Timber.d("查询倾角计信息：%s", command);
        commandItems.add(command);
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        sendCommandFromCmdList(() -> {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(true);
            }
        });
        String tempStr = cmdStr.replace(CommandResult.COMMAND_RESULT_HEADER, "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DAS_STATUS_1://##041\r\n：查询设备状态1
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询设备状态1指令出错!");
                    ToastUtils.show("查询设备状态1出错!");
                    return;
                }
                DeviceStatusInfoOne deviceStatusInfoOne = ResultParserUtil.getEntityObject(cmdStr);
                initDeviceStatusOne(deviceStatusInfoOne);
                break;

            case VERSION_MESSAGE://##040\r\n：获取版本信息
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询设备版本信息出错!");
                    ToastUtils.show("查询设备版本信息出错!");
                    return;
                }
                versionMessageInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (versionMessageInfo != null) {
                    mTvFirmwareVersion.setText(versionMessageInfo.getFirmwareVersion());
                }
                break;

            case SYSTEM_RUN_STATE://##014\r\n：获取信号强度
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询运行状态出错!");
                    ToastUtils.show("查询运行状态出错!");
                    return;
                }
                SystemRunStateInfo runStateInfo = ResultParserUtil.getEntityObject(cmdStr);
                initOperatorInformation(runStateInfo);
                break;

            case QUERY_NETWORK_STATUS://##044n\r\n：数据中心网络状态
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询数据中心网络状态出错!");
                    ToastUtils.show("查询数据中心网络状态出错!");
                    return;
                }
                String number = tempStr.substring(3, 4);
                if ("1".equals(number)) {
                    dataCenterStatusInfoList.clear();
                    dataCenterAdapter.notifyDataSetChanged();
                }
                DeviceNetStatus deviceNetStatus = ResultParserUtil.getEntityObject(cmdStr);
                dataCenterStatusInfoList.add(deviceNetStatus);
                dataCenterAdapter.notifyItemInserted(dataCenterStatusInfoList.size());
                break;

            case QUERY_DAS_STATUS_2://##042\r\n：查询设备状态2
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询设备状态2出错!");
                    ToastUtils.show("查询设备状态2出错!");
                    return;
                }
                DeviceStatusInfoTwo statusTwo = ResultParserUtil.getEntityObject(cmdStr);
                setDeviceStatusTwo(statusTwo);
                break;

            case QUERY_DAS_STATUS_3://##043\r\n: 获取主传感器状态
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询设备状态3出错!");
                    ToastUtils.show("查询设备状态3出错!");
                    return;
                }
                DeviceStatusInfoThree statusThree = ResultParserUtil.getEntityObject(cmdStr);
                if (statusThree != null) {
                    collectorModelStr = statusThree.getCollectorModel();
                    setDeviceStatusThree(statusThree);
                }
                break;

            case QUERY_INCLINOMETER_INFO:
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
     * 获取信号强度  ##014
     *
     * @param runStateInfo
     */
    private void initOperatorInformation(SystemRunStateInfo runStateInfo) {
        if (runStateInfo == null)
            return;
        mTvSignalStrength.setCompoundDrawablesWithIntrinsicBounds(0, 0, DeviceCurrentRunStateUtils.getSignalResIdByCSQValue(Integer.parseInt(runStateInfo.getGprsSignal())), 0);
        mTvSignalStrength.setText(DeviceCurrentRunStateUtils.getOperatorType2(runStateInfo.getOperator()));

        String internalVoltageStr;
        double internalVoltage;
        if (runStateInfo.getBatteryVoltage().contains("%")) {
            internalVoltageStr = runStateInfo.getBatteryVoltage();
            internalVoltage = Double.parseDouble(internalVoltageStr.replace("%", ""));
        } else {
            internalVoltage = Double.parseDouble(runStateInfo.getBatteryVoltage());
            internalVoltageStr = DeviceCurrentRunStateUtils.setDeviceInternalBattery(internalVoltage);
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(internalVoltageStr);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(internalVoltage <= 10 ? com.blankj.utilcode.util.ColorUtils.getColor(R.color.red) : com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvDeviceInternalPower.setText(builder);

        String externalVoltageStr = runStateInfo.getExternalVoltage();
        double externalVoltage = Double.parseDouble(externalVoltageStr);
        builder = new SpannableStringBuilder(externalVoltageStr + "V");
        colorSpan = new ForegroundColorSpan(externalVoltage <= 5 ? com.blankj.utilcode.util.ColorUtils.getColor(R.color.red) : com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvDeviceExternalVoltage.setText(builder);
    }

    /**
     * 设置设备状态2  ##042
     *
     * @param statusTwo
     */
    private void setDeviceStatusTwo(DeviceStatusInfoTwo statusTwo) {
        if (statusTwo == null)
            return;

        //设备经纬度
        if (!TextUtils.isEmpty(statusTwo.getLongitude()) && !TextUtils.isEmpty(statusTwo.getLatitude())) {
            mTvInstallPosition.setText(statusTwo.getLongitude() + "," + statusTwo.getLatitude());
        }

        //太阳能控制器
        solarInfoLayout.setVisibility(View.VISIBLE);
        setDeviceStatus(mTvSolarStatus, statusTwo.getSolarControllerStatus());
        decimalFormat.applyPattern("#.#");
        double dd = Double.parseDouble(statusTwo.getSolarPanelVoltage());
        mTvSolarVoltage.setText(decimalFormat.format(Double.parseDouble(statusTwo.getSolarPanelVoltage())) + "V");
        mTvBatteryVoltage.setText(decimalFormat.format(Double.parseDouble(statusTwo.getBatteryVoltage())) + "V");
        mTvSupplyPower.setText(decimalFormat.format(Double.parseDouble(statusTwo.getDailyPowerGeneration())) + "W");
        mTvConsumePower.setText(decimalFormat.format(Double.parseDouble(statusTwo.getDailyPowerConsumption())) + "W");

        //机箱内部温湿度
        internalTempHumidityLayout.setVisibility(View.VISIBLE);
        setDeviceStatus(mTvInternalStatus, statusTwo.getInternalTempHumidityStatus());
        decimalFormat.applyPattern("#.#");
        mTvInternalTemperature.setText(decimalFormat.format(Double.parseDouble(statusTwo.getInternalTemperature())) + "°");
        mTvInternalHumidity.setText(decimalFormat.format(Double.parseDouble(statusTwo.getInternalHumidity())) + "%");

        //机箱外部温湿度
        externalTempHumidityLayout.setVisibility(View.VISIBLE);
        setDeviceStatus(mTvExternalStatus, statusTwo.getExternalTempHumidityStatus());
        decimalFormat.applyPattern("#.#");
        mTvExternalTemperature.setText(decimalFormat.format(Double.parseDouble(statusTwo.getExternalTemperature())) + "°");
        mTvExternalHumidity.setText(decimalFormat.format(Double.parseDouble(statusTwo.getExternalHumidity())) + "%");

        //雨量值 断线报警器状态
        //根据开关量类型来判断降雨量和断线报警器
        ioSensorLayout.setVisibility(View.VISIBLE);
        String type = statusTwo.getSwitchType();
        switch (type) {
            case "1":
                mTvSwitchStatus.setText("接入");
                mTvSwitchStatus.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
                rainLayout.setVisibility(View.VISIBLE);
                wireBreakAlarmLayout.setVisibility(View.GONE);

                decimalFormat.applyPattern("#.#");
                mTvRain.setText(decimalFormat.format(Double.parseDouble(statusTwo.getRainfallStatus())) + "mm");
                break;

            case "2":
                mTvSwitchStatus.setText("未接入");
                mTvSwitchStatus.setTextColor(Color.RED);
                rainLayout.setVisibility(View.GONE);
                wireBreakAlarmLayout.setVisibility(View.GONE);
                break;

            case "3":
                mTvSwitchStatus.setText("接入");
                mTvSwitchStatus.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
                rainLayout.setVisibility(View.GONE);
                wireBreakAlarmLayout.setVisibility(View.VISIBLE);

                if (statusTwo.getRainfallStatus().equals("1.0") || statusTwo.getRainfallStatus().equals("1")) {
                    mTvAlarmStatus.setText("断线");
                    mTvAlarmStatus.setTextColor(Color.RED);
                } else {
                    mTvAlarmStatus.setText("未断线");
                    mTvAlarmStatus.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
                }
                break;
        }
    }

    /**
     * 获取主传感器状态
     *
     * @param statusThree
     */
    private void setDeviceStatusThree(DeviceStatusInfoThree statusThree) {
        if (statusThree == null)
            return;

        if (statusThree.getCollectorAddress().equals("0") || statusThree.getSensorStatus().isEmpty()) {
            mainSensorInfoLayout.setVisibility(View.GONE);
            return;
        }
        mainSensorInfoLayout.setVisibility(View.VISIBLE);
        sensorList.clear();
        sensorList.addAll(statusThree.getSensorStatus());
        sensorAdapter.notifyDataSetChanged();
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
        if (!TextUtils.isEmpty(inclinometerInfo.getxAxis()) && !TextUtils.isEmpty(inclinometerInfo.getyAxis()) && !TextUtils.isEmpty(inclinometerInfo.getzAxis())) {
            inclinometerAxisLayout.setVisibility(View.VISIBLE);
            mTvAxisX.setText(MessageFormat.format("{0}", inclinometerInfo.getxAxis()));
            mTvAxisY.setText(MessageFormat.format("{0}", inclinometerInfo.getyAxis()));
            mTvAxisZ.setText(MessageFormat.format("{0}", inclinometerInfo.getzAxis()));
        }

        if (!TextUtils.isEmpty(inclinometerInfo.getxAcceleration()) && !TextUtils.isEmpty(inclinometerInfo.getyAcceleration()) && !TextUtils.isEmpty(inclinometerInfo.getzAcceleration())) {
            inclinometerAccelerationLayout.setVisibility(View.VISIBLE);
            mTvAccelerationX.setText(MessageFormat.format("{0}", inclinometerInfo.getxAcceleration()));
            mTvAccelerationY.setText(MessageFormat.format("{0}", inclinometerInfo.getyAcceleration()));
            mTvAccelerationZ.setText(MessageFormat.format("{0}", inclinometerInfo.getzAcceleration()));
        }
    }

    private void setDeviceStatus(TextView textView, String status) {
        if (status.equals("1")) {
            textView.setText("未接入");
            textView.setTextColor(Color.RED);
        } else if (status.equals("0")) {
            textView.setText("正常");
            textView.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
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

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
