package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

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
import com.shmedo.configlibrary.iot.model.hac.HacMeasuringDataInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class BleAdmeHacMeasuringHoleDepthFragment extends BaseUSRBleIotCommunicateFragment {


    public static BleAdmeHacMeasuringHoleDepthFragment newInstance() {
        return new BleAdmeHacMeasuringHoleDepthFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_adme_hac_measuring_hole_depth;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryBasicParamConfigInfo();
    }

    /**
     * 获取设备的基础配置参数
     */
    private void queryBasicParamConfigInfo() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_GET_DATA_MEASURE_PARAM);
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
            case ADME_HAC_MD_GET_DATA_MEASURE_PARAM: {//获取设备的基础配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<HacMeasuringDataInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的基础配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
//                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                HacMeasuringDataInfo measuringDataInfo = commandResult.getResult();
                ToastUtils.show("接收完成");
            }
            break;

        }
    }

}