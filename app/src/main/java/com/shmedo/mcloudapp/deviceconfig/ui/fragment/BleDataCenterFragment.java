package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.CmdResponseMessage;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.mcloudapp.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.OnClick;

/**
 * 蓝牙配置数据中心
 */
public class BleDataCenterFragment extends BaseBleConnectFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_data_center;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        setFilter();
//        queryCollectorInfo();
    }


    @OnClick({R.id.btn_confirm})
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
//            sendCollector();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        super.onMessageEvent(messageEvent);

        if (messageEvent instanceof CmdResponseMessage) {
            setResultData((CmdResponseMessage) messageEvent);
        }
    }

    private void setResultData(CmdResponseMessage responseMessage) {
        String cmdStr = responseMessage.getResult();
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case COLLECTOR_CONFIG://采集器配置信息 100
//                if (tempStr.endsWith(CommandResult.ERROR_END)) {
//                    stopProgressRunnable();
//                    Timber.e("查询采集器配置信息指令出错!");
//                    return;
//                }
//                stopProgressRunnable();
//                collectorConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
//                initValue();
                break;
        }
    }
}
