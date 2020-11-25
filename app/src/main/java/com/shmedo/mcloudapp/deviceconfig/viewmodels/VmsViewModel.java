package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.configlibrary.iot.model.TerminalInfo;
import com.shmedo.mcloudapp.deviceconfig.data.repository.DeviceRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：     TODO
 */
public class VmsViewModel extends ViewModel {
    private final UnPeekLiveData<List<TerminalInfo>> VmsTerminalListLiveData = new UnPeekLiveData<>();
    private UnPeekLiveData<Boolean> vmsRefreshTerminal;

    public ProtectedUnPeekLiveData<String> getDeviceApiKey() {
        return DeviceRepository.getInstance().getDeviceApiKeyLiveData();
    }

    public void clearDeviceApiKey() {
        DeviceRepository.getInstance().clearDeviceApiKey();
    }

    public String getDeviceApiKeyBySn(String sn) {
        return DeviceRepository.getInstance().getDeviceApiKeyBySn(sn);
    }


    public ProtectedUnPeekLiveData<List<TerminalInfo>> getVmsTerminalList() {
        return VmsTerminalListLiveData;
    }

    public void addTerminalList(List<TerminalInfo> tempList) {
        if (tempList == null || tempList.size() == 0)
            return;

        List<TerminalInfo> cacheList = VmsTerminalListLiveData.getValue();
        if (cacheList == null)
            cacheList = new ArrayList<>();

        cacheList.addAll(tempList);
        VmsTerminalListLiveData.postValue(cacheList);
    }

    public void clearTerminalList() {
        VmsTerminalListLiveData.postValue(null);
    }


    public ProtectedUnPeekLiveData<Boolean> getVmsRefreshTerminal() {
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
