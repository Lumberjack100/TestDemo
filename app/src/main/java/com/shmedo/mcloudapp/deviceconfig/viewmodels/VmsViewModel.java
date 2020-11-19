package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.configlibrary.iot.model.TerminalBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：     TODO
 */
public class VmsViewModel extends ViewModel {
    private final UnPeekLiveData<String> deviceApiKey = new UnPeekLiveData<>();
    private final UnPeekLiveData<List<TerminalBean>> VmsTerminalListLiveData = new UnPeekLiveData<>();
    private UnPeekLiveData<Boolean> vmsRefreshTerminal;

    public UnPeekLiveData<String> getDeviceApiKey() {
        return deviceApiKey;
    }

    public void updateDeviceApiKey(String appKey) {
        deviceApiKey.postValue(appKey);
    }

    public void clearDeviceApiKey() {
        deviceApiKey.postValue(null);

    }


    public ProtectedUnPeekLiveData<List<TerminalBean>> getVmsTerminalList() {
        return VmsTerminalListLiveData;
    }

    public void addTerminalList(List<TerminalBean> tempList) {
        if (tempList == null || tempList.size() == 0)
            return;

        List<TerminalBean> cacheList = VmsTerminalListLiveData.getValue();
        if (cacheList == null)
            cacheList = new ArrayList<>();

        cacheList.addAll(tempList);
        VmsTerminalListLiveData.postValue(cacheList);
    }

    public void clearTerminalList() {
        VmsTerminalListLiveData.postValue(null);
    }


    public UnPeekLiveData<Boolean> getVmsRefreshTerminal() {
        if (vmsRefreshTerminal == null) {
            vmsRefreshTerminal = new UnPeekLiveData<>();
            vmsRefreshTerminal.setValue(false);
        }
        return vmsRefreshTerminal;
    }

    public void setVmsRefreshTerminal(boolean isRefresh) {
        vmsRefreshTerminal.postValue(isRefresh);
    }
}
