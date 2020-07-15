package com.shmedo.mcloudapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.shmedo.mcloudapp.event.NetworkChangeEvent;
import com.shmedo.mcloudapp.util.NetworkUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.receiver
 * 创建者:   gonghe
 * 创建时间:  2019-10-18
 * 描述：  监听网络状态变更的广播接收器
 */
public class NetworkConnectChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION) {
            /*判断当前网络时候可用以及网络类型*/
            boolean isConnected = NetworkUtils.isConnected();
            NetworkUtils.NetworkType networkType = NetworkUtils.getNetworkType();
            EventBus.getDefault().post(new NetworkChangeEvent(isConnected, networkType));
        }
    }
}