package com.shmedo.mcloudapp.common.viewmodels;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.core.event.NetworkChangeEvent;
import com.shmedo.core.util.NetworkUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/5 <br/>
 * 描述：     TODO
 */
public class NetWorkManager {

    private Context mContext;

    private final UnPeekLiveData<NetworkChangeEvent> networkChangeEventLiveData = new UnPeekLiveData<>();


    public NetWorkManager(Context mContext) {
        this.mContext = mContext;

        //注册网络状态监听广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mNetWorkChangReceiver,filter);
    }

    public final ProtectedUnPeekLiveData<NetworkChangeEvent> getNetworkChangeEvent() {
        return networkChangeEventLiveData;
    }

    private final BroadcastReceiver mNetWorkChangReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION) {
                /*判断当前网络是否可用以及网络类型*/
                boolean isConnected = NetworkUtils.isConnected();
                NetworkUtils.NetworkType networkType = NetworkUtils.getNetworkType();
                networkChangeEventLiveData.postValue(new NetworkChangeEvent(isConnected, networkType));
            }
        }
    };

    public void close() {
        try {
            mContext.unregisterReceiver(mNetWorkChangReceiver);
        } catch (final Exception e) {
            // The receiver must have been already unregistered before.
        }
    }
}
