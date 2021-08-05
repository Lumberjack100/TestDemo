package com.shmedo.mcloudapp.deviceconfig.ui.fragment.rn20;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.das.DasSubSensorStatusInfo;
import com.shmedo.configlibrary.iot.model.das.DasTemperatureAndHumidityStatusinfo;
import com.shmedo.configlibrary.iot.model.rn20.Rn20BaseInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
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
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.refresh_failed_while_device_disconnected));
                    mRefreshLayout.finishRefresh(false);
                    return;
                }
                queryBaseInfo();
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()) {
                            refreshLayout.finishRefresh(false);
                            ToastUtils.show("刷新超时");
                        }
                    }
                }, WRITE_TIME_OUT_MILLIS);
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
                    mRefreshLayout.finishRefresh(false);
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
                    mRefreshLayout.finishRefresh(false);
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
                mRefreshLayout.finishRefresh(true);
                IOTCommandResult<DasSubSensorStatusInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询辅传感器状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DasSubSensorStatusInfo dasSubSensorStatusInfo = commandResult.getResult();
                initSubSensorStatus(dasSubSensorStatusInfo);
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
            setDeviceStatus(mTvInternalStatus, dasTemperatureAndHumidityStatusinfo.getInth().getErrno());
            mTvInternalTemperature.setText(dasTemperatureAndHumidityStatusinfo.getInth().getTemp() + "°");
            mTvInternalHumidity.setText(dasTemperatureAndHumidityStatusinfo.getInth().getHumi() + "%");
        }
//        if (dasTemperatureAndHumidityStatusinfo.getOutth() != null) {
//            //机箱外部温湿度
//            setDeviceStatus(mTvExternalStatus, dasTemperatureAndHumidityStatusinfo.getOutth().getErrno());
//            mTvExternalTemperature.setText(dasTemperatureAndHumidityStatusinfo.getOutth().getTemp() + "°");
//            mTvExternalHumidity.setText(dasTemperatureAndHumidityStatusinfo.getOutth().getHumi() + "%");
//        }
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
            if (dasSubSensorStatusInfo.getIo().getType() == 1) {
                mTvRain.setText(dasSubSensorStatusInfo.getIo().getVaule() + "mm");
            }
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

}
