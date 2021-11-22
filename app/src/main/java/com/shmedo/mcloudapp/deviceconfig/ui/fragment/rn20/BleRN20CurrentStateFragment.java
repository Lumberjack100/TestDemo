package com.shmedo.mcloudapp.deviceconfig.ui.fragment.rn20;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.TerminalSNEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.das.DasSubSensorStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasTemperatureAndHumidityStatusinfo;
import com.shmedo.configlibrary.iot.model.rn20.Rn20BaseInfo;
import com.shmedo.configlibrary.iot.model.rn20.Rn20ModuleStatus;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/3 <br/>
 * 描述：     TODO
 */
public class BleRN20CurrentStateFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    /**
     * 基本信息
     */
    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_net_id)
    TextView mTvNetId;

    @BindView(R.id.tv_address)
    TextView mTvAddress;

    @BindView(R.id.tv_channel)
    TextView mTvChannel;

    @BindView(R.id.tv_install_position)
    TextView mTvPosition;

    @BindView(R.id.tv_register_time)
    TextView mTvRegisterTime;

    @BindView(R.id.tv_update_time)
    TextView mTvUpdateTime;

    /**
     * 内部模块状态
     */
    @BindView(R.id.ivExpand)
    ImageView ivExpand;

    @BindView(R.id.ll_internal_module_state)
    ViewGroup internalModuleStateLayout;

    @BindView(R.id.tv_falsh_module)
    TextView mTvFalshModule;//存储芯片

    @BindView(R.id.tv_voltage_module)
    TextView mTvVoltageModule;//电压采集

    @BindView(R.id.tv_ble_module)
    TextView mTvBleModule;//蓝牙

    @BindView(R.id.tv_lora_module)
    TextView mTvLoraModule;//Lora模块

    @BindView(R.id.tv_vibrating_wire_module)
    TextView mTvVibratingWireModule;//振弦采集模块

    @BindView(R.id.tv_acceleration_module)
    TextView mTvAccelerationModule;//加速度模块

    @BindView(R.id.tv_azimuth_module)
    TextView mTvAzimuthModule;//方位角模块

    @BindView(R.id.tv_inclination_module)
    TextView mTvInclinationModule;//倾角模块

    @BindView(R.id.tv_temperature_and_humidity_module)
    TextView mTvTemperatureAndHumidityModule;//温湿度模块

    @BindView(R.id.tv_rtc_clock_module)
    TextView mTvRtcClockModule;//rtc时钟

    @BindView(R.id.tv_current_detection_module)
    TextView mTvCurrentDetectionModule;//电流检测模块

    /**
     * 通讯状态
     */
    @BindView(R.id.tv_signal_strength)
    TextView mTvSignalStrength;

    @BindView(R.id.tv_send_data)
    TextView mTvSendData;

    @BindView(R.id.tv_receive_data)
    TextView mTvReceiveData;

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
     * 辅传感器
     */
    @BindView(R.id.tv_rain_value)
    TextView mTvRain;//雨量值


    public static BleRN20CurrentStateFragment newInstance() {
        return new BleRN20CurrentStateFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.rn20_current_state_fragment;
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
                queryBaseInfo();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_15000_MILLIS);
            }
        });
    }

    /**
     * 获取设备的基本信息
     */
    private void queryBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RN20_MD_GET_TERMINAL_BASE);
        sendCommand(command);
    }

    /**
     * 获取温湿度状态
     */
    private void queryTemperatureAndHumidityStatus() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS);
        sendCommand(command);
    }

    /**
     * 获取辅传感器状态
     */
    private void querySubSensorStatus() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS);
        sendCommand(command);
    }

    /**
     * 获取设备模块状态信息
     */
    private void queryModuleStatus() {
        TerminalSNEntity entity = new TerminalSNEntity(MCloudApp.getCurDeviceToken());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RN20_MD_GET_TERMINAL_MODULE_STATUS, entity);
        sendCommand(command);
    }

    @OnClick({R.id.ll_internal_module_title})
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_internal_module_title) {
            if (internalModuleStateLayout.getVisibility() == View.VISIBLE) {
                internalModuleStateLayout.setVisibility(View.GONE);
                ivExpand.setRotation(0f);
            } else {
                internalModuleStateLayout.setVisibility(View.VISIBLE);
                ivExpand.setRotation(180f);
            }
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case RN20_MD_GET_TERMINAL_BASE: {
                IOTCommandResult<Rn20BaseInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                Rn20BaseInfo rn20BaseInfo = commandResult.getResult();
                initBaseInfo(rn20BaseInfo);
                //获取温湿度状态
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
                queryModuleStatus();
            }
            break;

            case RN20_MD_GET_TERMINAL_MODULE_STATUS: {//获取设备模块状态信息
                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<Rn20ModuleStatus> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询设备模块状态信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                Rn20ModuleStatus moduleStatus = commandResult.getResult();
                initModuleStatus(moduleStatus);
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    /**
     * 基本信息
     *
     * @param rn20BaseInfo
     */
    private void initBaseInfo(Rn20BaseInfo rn20BaseInfo) {
        if (rn20BaseInfo == null) {
            Timber.e("Rn20BaseInfo 为空!");
            return;
        }
        try {
            mTvDeviceSn.setText(rn20BaseInfo.getSn());
            mTvNetId.setText(rn20BaseInfo.getNetid());
            mTvAddress.setText(rn20BaseInfo.getAddr());
            mTvChannel.setText(rn20BaseInfo.getChannel());
            mTvPosition.setText(rn20BaseInfo.getLocal());
            mTvRegisterTime.setText(rn20BaseInfo.getLogintime());
            mTvUpdateTime.setText(rn20BaseInfo.getFinaltime());

            mTvSignalStrength.setText(rn20BaseInfo.getSsi());
            mTvSendData.setText(rn20BaseInfo.getSendbuf());
            mTvReceiveData.setText(rn20BaseInfo.getRecvbuf());
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
                setSensorStatus(mTvInternalStatus, dasTemperatureAndHumidityStatusinfo.getInth().getErrno());
                mTvInternalTemperature.setText(dasTemperatureAndHumidityStatusinfo.getInth().getTemp() + "°");
                mTvInternalHumidity.setText(dasTemperatureAndHumidityStatusinfo.getInth().getHumi() + "%");
            }
//        if (dasTemperatureAndHumidityStatusinfo.getOutth() != null) {
//            //机箱外部温湿度
//            setDeviceStatus(mTvExternalStatus, dasTemperatureAndHumidityStatusinfo.getOutth().getErrno());
//            mTvExternalTemperature.setText(dasTemperatureAndHumidityStatusinfo.getOutth().getTemp() + "°");
//            mTvExternalHumidity.setText(dasTemperatureAndHumidityStatusinfo.getOutth().getHumi() + "%");
//        }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        try {
            if (dasSubSensorStatusInfo.getIo() != null) {
                if (dasSubSensorStatusInfo.getIo().getType() == 1) {
                    mTvRain.setText(dasSubSensorStatusInfo.getIo().getVaule() + "mm");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initModuleStatus(Rn20ModuleStatus moduleStatus) {
        if (moduleStatus == null) {
            Timber.e("Rn20ModuleStatus 为空!");
            return;
        }
        try {
            setModuleStatus(mTvFalshModule, moduleStatus.getFlash());
            setModuleStatus(mTvVoltageModule, moduleStatus.getAds());
            setModuleStatus(mTvBleModule, moduleStatus.getBle());
            setModuleStatus(mTvLoraModule, moduleStatus.getLora());
            setModuleStatus(mTvVibratingWireModule, moduleStatus.getVm501());
            setModuleStatus(mTvAccelerationModule, moduleStatus.getAdxl362());
            setModuleStatus(mTvAzimuthModule, moduleStatus.getMmc5883());
            setModuleStatus(mTvInclinationModule, moduleStatus.getScl3300());
            setModuleStatus(mTvTemperatureAndHumidityModule, moduleStatus.getAht21());
            setModuleStatus(mTvRtcClockModule, moduleStatus.getRtc());
            setModuleStatus(mTvCurrentDetectionModule, moduleStatus.getLtc2945());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSensorStatus(TextView textView, int status) {
        if (status == 0) {
            textView.setText("未接入");
            textView.setTextColor(Color.RED);
        } else if (status == 1) {
            textView.setText("正常");
            textView.setTextColor(ColorUtils.getColor(R.color.text_color_3AD094));
        }
    }

    private void setModuleStatus(TextView textView, String status) {
        if (TextUtils.isEmpty(status) || status.equals("0")) {
            textView.setText("异常");
            textView.setTextColor(Color.RED);
        } else {
            textView.setText("正常");
            textView.setTextColor(ColorUtils.getColor(R.color.text_color_3AD094));
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
