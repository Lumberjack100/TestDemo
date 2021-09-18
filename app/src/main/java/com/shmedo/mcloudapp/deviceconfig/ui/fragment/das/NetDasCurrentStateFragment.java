package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.graphics.Color;
import android.os.Bundle;
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

import com.google.gson.reflect.TypeToken;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.das.IndexEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.model.das.DasBaseInfo;
import com.shmedo.configlibrary.iot.model.das.DasNetStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasSensorStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasSolarStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasSubSensorStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasTemperatureAndHumidityStatusinfo;
import com.shmedo.configlibrary.iot.utils.IOTSensorUtil;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.util.DeviceCurrentRunStateUtils;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 通过网络下发指令查看设备当前运行状态
 */
public class NetDasCurrentStateFragment extends BaseNetIotCommunicateFragment {
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

    //数字式渗压计
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

    @BindView(R.id.tv_inclinometer_axis)
    TextView mTvInclinometerAxis;

    @BindView(R.id.mainSensorInfo)
    View mainSensorInfoLayout;

    @BindView(R.id.tv_mainSensor)
    TextView mTvMainSensor;

    @BindView(R.id.sensor_recyclerView)
    RecyclerView sensorRecyclerView;

    private List<DasNetStatusInfo> netStatusInfoList = new ArrayList<>();
    private List<DasSensorStatusInfo> sensorList = new ArrayList<>();
    private CommonAdapter dataCenterAdapter, sensorAdapter;

    private DecimalFormat decimalFormat = new DecimalFormat();

    public static NetDasCurrentStateFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetDasCurrentStateFragment fragment = new NetDasCurrentStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_das_current_state_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                queryDeviceBaseInfo();
            }
        });
    }

    private void initDataCenterAdapter() {
        dataCenterRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dataCenterRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, DensityUtil.Dp2Px(mActivity, 10f), getResources().getColor(R.color.transparent)));
        dataCenterAdapter = new CommonAdapter<DasNetStatusInfo>(getActivity(), R.layout.item_das_data_center_status, netStatusInfoList) {
            @Override
            protected void convert(CommonViewHolder holder, DasNetStatusInfo netStatusInfo, int position) {
                holder.setText(R.id.tv_center_title, "中心" + netStatusInfo.getIndex());
                holder.setText(R.id.tv_link_send_data, String.valueOf(netStatusInfo.getSend()));
                holder.setText(R.id.tv_link_unsend_data, String.valueOf(netStatusInfo.getUnsend()));
                holder.setText(R.id.tv_link_online_rate, netStatusInfo.getRate() + "%");

                //未开启
                if (netStatusInfo.getErrno() == 0) {
                    holder.setText(R.id.tv_link_status, "未开启");
                    holder.setTextColor(R.id.tv_link_status, Color.GRAY);
                    holder.setText(R.id.tv_link_send_data, "0");
                    holder.setText(R.id.tv_link_unsend_data, "0");
                    holder.setText(R.id.tv_link_online_rate, "0");

                } else if (netStatusInfo.getErrno() == 1) {//上线
                    holder.setText(R.id.tv_link_status, "已上线");
                    holder.setTextColorRes(R.id.tv_link_status, R.color.text_color_3AD094);

                } else if (netStatusInfo.getErrno() == 2) {//离线
                    holder.setText(R.id.tv_link_status, "离线");
                    holder.setTextColor(R.id.tv_link_status, Color.RED);
                }
            }
        };
        dataCenterRecyclerView.setAdapter(dataCenterAdapter);
    }

    private void initSensorAdapter() {
        sensorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sensorRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, DensityUtil.Dp2Px(mActivity, 10f), getResources().getColor(R.color.transparent)));
        sensorAdapter = new CommonAdapter<DasSensorStatusInfo>(getActivity(), R.layout.item_sensor_status, sensorList) {
            @Override
            protected void convert(CommonViewHolder holder, DasSensorStatusInfo sensorStatusInfo, int position) {
                String type = sensorStatusInfo.getType();
                type = type.charAt(0) == '0' ? type.substring(1) : type;
                IOTSensorType sensorType = IOTSensorType.value(type);
                switch (sensorType) {
                    case WIRE_SHIFT://拉绳式裂缝计
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_wire_shift));
                        holder.setVisibleOrGone(R.id.value2Layout, false);
                        holder.setVisibleOrGone(R.id.value3Layout, false);
                        holder.setText(R.id.tv_title1, "裂缝值(mm)");
                        decimalFormat.applyPattern("#");
                        holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(sensorStatusInfo.getVal())));
                        break;

                    case SOIL_MOISTURE://土壤含水率
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_soil_moisture));
                        holder.setVisibleOrGone(R.id.value2Layout, false);
                        holder.setVisibleOrGone(R.id.value3Layout, false);
                        holder.setText(R.id.tv_title1, "含水率(%)");
                        decimalFormat.applyPattern("#.#");
                        holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(sensorStatusInfo.getVal())));
                        break;

                    case INCLINOMETER: {//测斜仪
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_inclinometer));
                        holder.setVisibleOrGone(R.id.value2Layout, true);
                        holder.setVisibleOrGone(R.id.value3Layout, false);
                        holder.setText(R.id.tv_title1, "X轴(mm)");
                        holder.setText(R.id.tv_title2, "Y轴(mm)");
                        String[] values = sensorStatusInfo.getVal().split(",");
                        if (values.length >= 2) {
                            decimalFormat.applyPattern("#");
                            holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(values[0])));
                            holder.setText(R.id.tv_value2, decimalFormat.format(Double.parseDouble(values[1])));
                        }
                    }
                    break;

                    case ULTRASONIC_LEVEL_GAUGE://超声波物位计
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_ultrasonic_level_gauge));
                        holder.setVisibleOrGone(R.id.value2Layout, false);
                        holder.setVisibleOrGone(R.id.value3Layout, false);
                        holder.setText(R.id.tv_title1, "空高值(mm)");
                        decimalFormat.applyPattern("#");
                        holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(sensorStatusInfo.getVal())));
                        break;

                    case RADAR_LEVEL_GAUGE://雷达物位计
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_radar_level_gauge));
                        holder.setVisibleOrGone(R.id.value2Layout, false);
                        holder.setVisibleOrGone(R.id.value3Layout, false);
                        holder.setText(R.id.tv_title1, "空高值(mm)");
                        decimalFormat.applyPattern("#");
                        holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(sensorStatusInfo.getVal())));
                        break;

                    case UPLIFT_PRESSURE_GAUGE: {//数字式渗压计
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_uplift_pressure_gauge));
                        holder.setVisibleOrGone(R.id.value2Layout, true);
                        holder.setVisibleOrGone(R.id.value3Layout, true);
                        holder.setText(R.id.tv_title1, "水深(m)");
                        holder.setText(R.id.tv_title2, "空管(m)");
                        holder.setText(R.id.tv_title3, "水温(℃)");
                        String[] values = sensorStatusInfo.getVal().split(",");
                        if (values.length >= 3) {
                            decimalFormat.applyPattern("#.###");
                            holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(values[0])));
                            holder.setText(R.id.tv_value2, decimalFormat.format(Double.parseDouble(values[1])));
                            decimalFormat.applyPattern("#.#");
                            holder.setText(R.id.tv_value3, decimalFormat.format(Double.parseDouble(values[2])));
                        }
                    }
                    break;

                    case KANG_PERCOLATE: {//基康渗压计
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_kang_percolate));
                        holder.setVisibleOrGone(R.id.value2Layout, true);
                        holder.setVisibleOrGone(R.id.value3Layout, true);
                        holder.setText(R.id.tv_title1, "水深(m)");
                        holder.setText(R.id.tv_title2, "空管(m)");
                        holder.setText(R.id.tv_title3, "水温(℃)");
                        String[] values = sensorStatusInfo.getVal().split(",");
                        if (values.length >= 3) {
                            decimalFormat.applyPattern("#.###");
                            holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(values[0])));
                            holder.setText(R.id.tv_value2, decimalFormat.format(Double.parseDouble(values[1])));
                            decimalFormat.applyPattern("#.#");
                            holder.setText(R.id.tv_value3, decimalFormat.format(Double.parseDouble(values[2])));
                        }
                    }
                    break;

                    case GUDAN_PERCOLATE: {//葛南渗压计
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_gudan_percolate));
                        holder.setVisibleOrGone(R.id.value2Layout, true);
                        holder.setVisibleOrGone(R.id.value3Layout, true);
                        holder.setText(R.id.tv_title1, "水深(m)");
                        holder.setText(R.id.tv_title2, "空管(m)");
                        holder.setText(R.id.tv_title3, "水温(℃)");
                        String[] values = sensorStatusInfo.getVal().split(",");
                        if (values.length >= 3) {
                            decimalFormat.applyPattern("#.###");
                            holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(values[0])));
                            holder.setText(R.id.tv_value2, decimalFormat.format(Double.parseDouble(values[1])));
                            decimalFormat.applyPattern("#.#");
                            holder.setText(R.id.tv_value3, decimalFormat.format(Double.parseDouble(values[2])));
                        }
                    }
                    break;

                    case GUDAN_STRESS: {//葛南应变计
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_gudan_stress));
                        holder.setVisibleOrGone(R.id.value2Layout, true);
                        holder.setVisibleOrGone(R.id.value3Layout, false);
                        holder.setText(R.id.tv_title1, "应变(μ)");
                        holder.setText(R.id.tv_title2, "温度(℃)");
                        String[] values = sensorStatusInfo.getVal().split(",");
                        if (values.length >= 2) {
                            decimalFormat.applyPattern("#.#");
                            holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(values[0])));
                            holder.setText(R.id.tv_value2, decimalFormat.format(Double.parseDouble(values[1])));
                        }
                    }
                    break;

                    case JUNXING_ZLJ_300T: {//轴力计
                        holder.setText(R.id.tv_sensor_name, GlobalUtil.getString(R.string.sensor_junxing_zlj_300t));
                        holder.setVisibleOrGone(R.id.value2Layout, true);
                        holder.setVisibleOrGone(R.id.value3Layout, false);
                        holder.setText(R.id.tv_title1, "轴力(KN)");
                        holder.setText(R.id.tv_title2, "温度(℃)");
                        String[] values = sensorStatusInfo.getVal().split(",");
                        if (values.length >= 2) {
                            decimalFormat.applyPattern("#.#");
                            holder.setText(R.id.tv_value1, decimalFormat.format(Double.parseDouble(values[0])));
                            holder.setText(R.id.tv_value2, decimalFormat.format(Double.parseDouble(values[1])));
                        }
                    }
                    break;

                    default:
                        holder.setText(R.id.tv_value1, sensorStatusInfo.getVal() + "");
                        break;
                }
                try {
                    String sensorAisle = (sensorType == IOTSensorType.KANG_PERCOLATE ||
                            sensorType == IOTSensorType.GUDAN_PERCOLATE ||
                            sensorType == IOTSensorType.GUDAN_STRESS ||
                            sensorType == IOTSensorType.JUNXING_ZLJ_300T) ? String.valueOf(sensorStatusInfo.getAddr() + 1) : String.valueOf(sensorStatusInfo.getAddr());
                    holder.setText(R.id.tv_address, "通道" + sensorAisle);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                holder.setText(R.id.tv_status, IOTSensorUtil.getInstance().getErrorMessageByNo(String.valueOf(sensorStatusInfo.getErrno())));
                if (sensorStatusInfo.getErrno() == 0) {
                    holder.setTextColorRes(R.id.tv_status, R.color.text_color_3AD094);
                } else {
                    holder.setTextColorRes(R.id.tv_status, R.color.red);
                }
            }
        };
        sensorRecyclerView.setAdapter(sensorAdapter);
    }

    /**
     * 获取基本信息
     */
    private void queryDeviceBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_DEVICE_BASE);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取数据中心状态
     */
    private void queryNetStatus(int index) {
        IndexEntity entity = new IndexEntity(index);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_NET_STATUS, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取太阳能控制器状态
     */
    private void querySolarStatus() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_SOLAR_STATUS);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取温湿度状态
     */
    private void queryTemperatureAndHumidityStatus() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取主传感器状态
     */
    private void querySensorStatus(int index) {
        IndexEntity entity = new IndexEntity(index);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_SENSOR_STATUS, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取辅传感器状态
     */
    private void querySubSensorStatus() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS);
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
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case DAS_MD_GET_DEVICE_BASE: {
                IOTCommandResult<DasBaseInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DasBaseInfo dasBaseInfo = commandResult.getResult();
                initBaseInfo(dasBaseInfo);
                queryNetStatus(0);
            }
            break;

            case DAS_MD_GET_NET_STATUS: {
                IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询数据中心状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                if (!TextUtils.isEmpty(commandResult.getResult())) {
                    List<DasNetStatusInfo> tempList = GsonFactory.getGson().fromJson(commandResult.getResult(), new TypeToken<List<DasNetStatusInfo>>() {
                    }.getType());
                    netStatusInfoList.clear();
                    netStatusInfoList.addAll(tempList);
                    dataCenterAdapter.notifyDataSetChanged();
                }
                querySolarStatus();
            }
            break;

            case DAS_MD_GET_SOLAR_STATUS: {
                IOTCommandResult<DasSolarStatusInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询太阳能控制器状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DasSolarStatusInfo dasSolarStatusInfo = commandResult.getResult();
                initSolarStatus(dasSolarStatusInfo);
                queryTemperatureAndHumidityStatus();
            }
            break;

            case DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS: {
                IOTCommandResult<DasTemperatureAndHumidityStatusinfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询温湿度状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DasTemperatureAndHumidityStatusinfo dasTemperatureAndHumidityStatusinfo = commandResult.getResult();
                initTemperatureAndHumidityStatus(dasTemperatureAndHumidityStatusinfo);
                querySubSensorStatus();
            }
            break;

            case DAS_MD_GET_SUB_SENSOR_STATUS: {
                IOTCommandResult<DasSubSensorStatusInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询辅传感器状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DasSubSensorStatusInfo dasSubSensorStatusInfo = commandResult.getResult();
                initSubSensorStatus(dasSubSensorStatusInfo);
                querySensorStatus(0);
            }
            break;

            case DAS_MD_GET_SENSOR_STATUS: {
                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<List<DasSensorStatusInfo>> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询主传感器状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                if (commandResult.getResult() != null) {
                    sensorList.clear();
                    sensorList.addAll(commandResult.getResult());
                    mainSensorInfoLayout.setVisibility(sensorList.size() > 0 ? View.VISIBLE : View.GONE);
                    sensorAdapter.notifyDataSetChanged();
                }
            }
            break;

            default:
                break;
        }
    }

    /**
     * 基本信息
     *
     * @param dasBaseInfo
     */
    private void initBaseInfo(DasBaseInfo dasBaseInfo) {
        if (dasBaseInfo == null) {
            Timber.e("DasBaseInfo 为空!");
            return;
        }
        mTvDeviceSn.setText(dasBaseInfo.getSn());
        mTVSimCardNumber.setText(dasBaseInfo.getIccid());
        mTvImeiNumber.setText(dasBaseInfo.getImei());
        mTvDeviceStartCode.setText(dasBaseInfo.getCode());
        mTvFirmwareVersion.setText(dasBaseInfo.getVer());
        mTvInstallPosition.setText(dasBaseInfo.getLocal());

        mTvSignalStrength.setCompoundDrawablesWithIntrinsicBounds(0, 0, DeviceCurrentRunStateUtils.getSignalResIdByCSQValue(Integer.parseInt(dasBaseInfo.getCsq())), 0);
        mTvSignalStrength.setText(DeviceCurrentRunStateUtils.getOperatorType(dasBaseInfo.getIsp()));

        String powerStr = dasBaseInfo.getInvolt();
        double power = Double.parseDouble(powerStr.replace("%", ""));
        SpannableStringBuilder builder = new SpannableStringBuilder(powerStr);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(power <= 10 ? getContext().getResources().getColor(R.color.red) : getContext().getResources().getColor(R.color.text_color_3AD094));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvDeviceInternalPower.setText(builder);

        String voltageStr = dasBaseInfo.getOutvolt();
        double voltage = Double.parseDouble(voltageStr);
        builder = new SpannableStringBuilder(voltageStr + "V");
        colorSpan = new ForegroundColorSpan(voltage <= 5 ? getContext().getResources().getColor(R.color.red) : getContext().getResources().getColor(R.color.text_color_3AD094));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvDeviceExternalVoltage.setText(builder);
    }

    /**
     * 太阳能控制器
     *
     * @param dasSolarStatusInfo
     */
    private void initSolarStatus(DasSolarStatusInfo dasSolarStatusInfo) {
        if (dasSolarStatusInfo == null) {
            Timber.e("DasSolarStatusInfo 为空!");
            return;
        }
        if (dasSolarStatusInfo.getSolar() == null) {
            Timber.e("SolarBean 为空!");
            return;
        }
        solarInfoLayout.setVisibility(View.VISIBLE);
        setDeviceStatus(mTvSolarStatus, dasSolarStatusInfo.getSolar().getErrno());
        decimalFormat.applyPattern("#.#");
        mTvSolarVoltage.setText(decimalFormat.format(dasSolarStatusInfo.getSolar().getSolarvolt()) + "V");
        mTvBatteryVoltage.setText(decimalFormat.format(dasSolarStatusInfo.getSolar().getBatvolt()) + "V");
        mTvSupplyPower.setText(decimalFormat.format(dasSolarStatusInfo.getSolar().getSolarpwr()) + "W");
        mTvConsumePower.setText(decimalFormat.format(dasSolarStatusInfo.getSolar().getLoadpwr()) + "W");
    }

    /**
     * 温湿度状态
     *
     * @param dasTemperatureAndHumidityStatusinfo
     */
    private void initTemperatureAndHumidityStatus(DasTemperatureAndHumidityStatusinfo dasTemperatureAndHumidityStatusinfo) {
        if (dasTemperatureAndHumidityStatusinfo == null) {
            Timber.e("DasTemperatureAndHumidityStatusinfo 为空!");
            return;
        }
        if (dasTemperatureAndHumidityStatusinfo.getInth() != null) {
            //机箱内部温湿度
            internalTempHumidityLayout.setVisibility(View.VISIBLE);
            setDeviceStatus(mTvInternalStatus, dasTemperatureAndHumidityStatusinfo.getInth().getErrno());
            decimalFormat.applyPattern("#.#");
            mTvInternalTemperature.setText(decimalFormat.format(dasTemperatureAndHumidityStatusinfo.getInth().getTemp()) + "℃");
            mTvInternalHumidity.setText(decimalFormat.format(dasTemperatureAndHumidityStatusinfo.getInth().getHumi()) + "%");
        }

        if (dasTemperatureAndHumidityStatusinfo.getOutth() != null) {
            //机箱外部温湿度
            externalTempHumidityLayout.setVisibility(View.VISIBLE);
            setDeviceStatus(mTvExternalStatus, dasTemperatureAndHumidityStatusinfo.getOutth().getErrno());
            decimalFormat.applyPattern("#.#");
            mTvExternalTemperature.setText(decimalFormat.format(dasTemperatureAndHumidityStatusinfo.getOutth().getTemp()) + "℃");
            mTvExternalHumidity.setText(decimalFormat.format(dasTemperatureAndHumidityStatusinfo.getOutth().getHumi()) + "%");
        }
    }

    /**
     * 辅传感器状态
     *
     * @param dasSubSensorStatusInfo
     */
    private void initSubSensorStatus(DasSubSensorStatusInfo dasSubSensorStatusInfo) {
        if (dasSubSensorStatusInfo == null) {
            Timber.e("DasSubSensorStatusInfo 为空!");
            return;
        }

        if (dasSubSensorStatusInfo.getIo() != null) {
            ioSensorLayout.setVisibility(View.VISIBLE);

            if (dasSubSensorStatusInfo.getIo().getType() == 1) {
                mTvSwitchStatus.setText("接入");
                mTvSwitchStatus.setTextColor(GlobalUtil.getColor(R.color.text_color_3AD094));
                rainLayout.setVisibility(View.VISIBLE);
                wireBreakAlarmLayout.setVisibility(View.GONE);

                decimalFormat.applyPattern("#.#");
                mTvRain.setText(decimalFormat.format(dasSubSensorStatusInfo.getIo().getVaule()) + "mm");

            } else if (dasSubSensorStatusInfo.getIo().getType() == 2) {
                mTvSwitchStatus.setText("未接入");
                mTvSwitchStatus.setTextColor(Color.RED);
                rainLayout.setVisibility(View.GONE);
                wireBreakAlarmLayout.setVisibility(View.GONE);

            } else if (dasSubSensorStatusInfo.getIo().getType() == 3) {
                mTvSwitchStatus.setText("接入");
                mTvSwitchStatus.setTextColor(GlobalUtil.getColor(R.color.text_color_3AD094));
                rainLayout.setVisibility(View.GONE);
                wireBreakAlarmLayout.setVisibility(View.VISIBLE);

                if (dasSubSensorStatusInfo.getIo().getVaule() == 1) {
                    mTvAlarmStatus.setText("断线");
                    mTvAlarmStatus.setTextColor(Color.RED);
                } else if (dasSubSensorStatusInfo.getIo().getVaule() == 0) {
                    mTvAlarmStatus.setText("未断线");
                    mTvAlarmStatus.setTextColor(getResources().getColor(R.color.text_color_3AD094));
                }
            }
        }

        if (dasSubSensorStatusInfo.getVwp() != null) {
            dasDigitalPiezometerLayout.setVisibility(View.VISIBLE);
            if (dasSubSensorStatusInfo.getVwp().getType() == 15) {
                mTvPiezometerTitle.setText("数字式渗压计");
            }
            mTvPiezometerStatus.setText(IOTSensorUtil.getInstance().getErrorMessageByNo(String.valueOf(dasSubSensorStatusInfo.getVwp().getErrno())));
            setSensorStatusColor(mTvPiezometerStatus, dasSubSensorStatusInfo.getVwp().getErrno());
            if (dasSubSensorStatusInfo.getVwp().getValue().contains(",")) {
                String[] values = dasSubSensorStatusInfo.getVwp().getValue().split(",");
                if (values.length >= 3) {
                    emptyPipeDistanceLayout.setVisibility(View.VISIBLE);
                    waterTemperatureLayout.setVisibility(View.VISIBLE);

                    decimalFormat.applyPattern("#.###");
                    mTvPiezometerValue.setText(decimalFormat.format(Double.parseDouble(values[0])) + "m");
                    mTvEmptyPipeDistance.setText(decimalFormat.format(Double.parseDouble(values[1])) + "m");

                    decimalFormat.applyPattern("#.#");
                    mTvWaterTemperature.setText(decimalFormat.format(Double.parseDouble(values[2])) + "℃");
                }
            } else {
                mTvPiezometerValue.setText(dasSubSensorStatusInfo.getVwp().getValue());
            }
        }

        if (dasSubSensorStatusInfo.getMems() != null) {
            inclinometerLayout.setVisibility(View.VISIBLE);
            if (dasSubSensorStatusInfo.getMems().getType() == 1) {
                mTvMemsTitle.setText("倾角计");
            }
            mTvInclinometerStatus.setText(IOTSensorUtil.getInstance().getErrorMessageByNo(String.valueOf(dasSubSensorStatusInfo.getMems().getErrno())));
            setSensorStatusColor(mTvInclinometerStatus, dasSubSensorStatusInfo.getMems().getErrno());

            decimalFormat.applyPattern("#.#");
            mTvInclinometerAxis.setText(decimalFormat.format(Double.parseDouble(dasSubSensorStatusInfo.getMems().getValue())) + "°");
        }
    }

    private void setDeviceStatus(TextView textView, int status) {
        if (status == 0) {
            textView.setText("未接入");
            textView.setTextColor(Color.RED);
        } else if (status == 1) {
            textView.setText("正常");
            textView.setTextColor(GlobalUtil.getColor(R.color.text_color_3AD094));
        }
    }

    private void setSensorStatusColor(TextView textView, int status) {
        if (status == 0) {
            textView.setTextColor(GlobalUtil.getColor(R.color.text_color_3AD094));
        } else {
            textView.setTextColor(Color.RED);
        }
    }
}
