package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shmedo.core.event.BluetoothConnectStateEvent;
import com.shmedo.core.event.CmdResponseMessage;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.mcloudapp.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 自定义蓝牙指令交互输出并保存日志文件
 */
public class BleCustomCommandLogPrintFragment extends BaseBleConnectFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_custom_command_log_print;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        super.onMessageEvent(messageEvent);

//        if (messageEvent instanceof CmdResponseMessage) {
//            setResultData((CmdResponseMessage) messageEvent);
//        } else if (messageEvent instanceof BluetoothConnectStateEvent) {
//            boolean isConnected = ((BluetoothConnectStateEvent) messageEvent).isConnected;
//            updateViewStateByConnectState(isConnected);
//        }
    }

}
