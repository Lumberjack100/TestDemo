package com.shmedo.mcloudapp.deviceconfig.ui.fragment.ac10;

import android.os.Bundle;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.ac10.AdmeAC10MeasuringDataInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class BleAdmeAC10MeasuringDataFragment extends BaseUSRBleIotCommunicateFragment {


    public static BleAdmeAC10MeasuringDataFragment newInstance() {
        return new BleAdmeAC10MeasuringDataFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_adme_ac10_measuring_data;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        queryBasicParamConfigInfo();
        queryEquipmentState();
        queryMotorState();
    }

    /**
     * 获取数据测量配置参数
     */
    private void queryBasicParamConfigInfo() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_AC10_GET_DATA_MEASURE_PARAM);
        sendCommand(command);
    }

    /**
     * 获取设备的当前状态
     */
    private void queryEquipmentState() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE);
        sendCommand(command);
    }

    /**
     * 获取电机的运行状态
     */
    private void queryMotorState() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MOTION_STATE);
        sendCommand(command);
    }


    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("响应超时,请稍后尝试");
                break;
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_AC10_GET_DATA_MEASURE_PARAM: {//获取数据测量配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeAC10MeasuringDataInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
//                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                AdmeAC10MeasuringDataInfo measuringDataInfo = commandResult.getResult();
                ToastUtils.show("接收完成");
            }
            break;

        }
    }

}