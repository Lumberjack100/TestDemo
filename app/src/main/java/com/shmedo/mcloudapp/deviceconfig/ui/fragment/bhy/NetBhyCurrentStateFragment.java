package com.shmedo.mcloudapp.deviceconfig.ui.fragment.bhy;

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

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
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
import com.shmedo.configlibrary.iot.model.das.DasBaseInfo;
import com.shmedo.configlibrary.iot.model.das.DasNetStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasSensorStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasSolarStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasSubSensorStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasTemperatureAndHumidityStatusinfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.util.DeviceCurrentRunStateUtils;
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
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/9 <br/>
 * 描述：     4G模式 BHY 设备运行状态页面
 */
public class NetBhyCurrentStateFragment extends BaseNetIotCommunicateFragment {
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
     * 传感器
     */
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

    public static NetBhyCurrentStateFragment newInstance(DeviceInfo deviceInfo) {
        NetBhyCurrentStateFragment fragment = new NetBhyCurrentStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.bhy_current_state_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDataCenterAdapter();
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
        dataCenterRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(10f), getResources().getColor(R.color.transparent)));
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

    /**
     * 获取基本信息
     */
    private void queryDeviceBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_DEVICE_BASE);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 获取数据中心状态
     */
    private void queryNetStatus(int index) {
        IndexEntity entity = new IndexEntity(index);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_NET_STATUS, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 获取太阳能控制器状态
     */
    private void querySolarStatus() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_SOLAR_STATUS);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 获取温湿度状态
     */
    private void queryTemperatureAndHumidityStatus() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }


    /**
     * 获取传感器信息
     */
    private void querySensorInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS);
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
                    List<DasNetStatusInfo> tempList = GsonUtils.fromJson(commandResult.getResult(), new TypeToken<List<DasNetStatusInfo>>() {
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
                querySensorInfo();
            }
            break;

            case DAS_MD_GET_SUB_SENSOR_STATUS: {
                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<DasSubSensorStatusInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询辅传感器状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DasSubSensorStatusInfo dasSubSensorStatusInfo = commandResult.getResult();
//                initSubSensorStatus(dasSubSensorStatusInfo);
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
        try {
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        try {
            solarInfoLayout.setVisibility(View.VISIBLE);
            setDeviceStatus(mTvSolarStatus, dasSolarStatusInfo.getSolar().getErrno());
            decimalFormat.applyPattern("#.#");
            mTvSolarVoltage.setText(decimalFormat.format(dasSolarStatusInfo.getSolar().getSolarvolt()) + "V");
            mTvBatteryVoltage.setText(decimalFormat.format(dasSolarStatusInfo.getSolar().getBatvolt()) + "V");
            mTvSupplyPower.setText(decimalFormat.format(dasSolarStatusInfo.getSolar().getSolarpwr()) + "W");
            mTvConsumePower.setText(decimalFormat.format(dasSolarStatusInfo.getSolar().getLoadpwr()) + "W");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        try {
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setDeviceStatus(TextView textView, int status) {
        if (status == 0) {
            textView.setText("未接入");
            textView.setTextColor(Color.RED);
        } else if (status == 1) {
            textView.setText("正常");
            textView.setTextColor(ColorUtils.getColor(R.color.text_color_3AD094));
        }
    }

}
