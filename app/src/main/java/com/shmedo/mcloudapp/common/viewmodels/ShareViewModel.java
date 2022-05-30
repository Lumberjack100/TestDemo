package com.shmedo.mcloudapp.common.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.shmedo.core.event.ForceToLoginEvent;
import com.shmedo.core.event.NetworkChangeEvent;
import com.shmedo.mcloudapp.util.ResponseHandler;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/5 <br/>
 * 描述：     TODO #gh#
 */
public class ShareViewModel extends AndroidViewModel {
    private final NetWorkManager netWorkManager;


    public ShareViewModel(@NonNull Application application) {
        super(application);
        netWorkManager = new NetWorkManager(application);
    }

    public ProtectedUnPeekLiveData<NetworkChangeEvent> getNetworkChangeEvent() {
        return netWorkManager.getNetworkChangeEvent();
    }

    public ProtectedUnPeekLiveData<ForceToLoginEvent> getForceToLoginEvent() {
        return ResponseHandler.getInstance().getForceToLoginEvent();
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        netWorkManager.close();
    }
}
