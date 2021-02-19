package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.mcloudapp.deviceconfig.data.repository.DeviceRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：     TODO #gh#
 */
public class VmsViewModel extends ViewModel {
    private final UnPeekLiveData<List<VmsTerminalInfo>> cacheVmsTerminalListLiveData = new UnPeekLiveData<>();
    private UnPeekLiveData<Boolean> vmsRefreshTerminal;

    public ProtectedUnPeekLiveData<String> getDeviceApiKey() {
        return DeviceRepository.getInstance().getDeviceApiKeyLiveData();
    }

    public void clearDeviceApiKey() {
        DeviceRepository.getInstance().clearDeviceApiKey();
    }

    public void queryDeviceApiKeyBySn(String sn) {
        DeviceRepository.getInstance().queryDeviceApiKeyBySn(sn);
    }


    public ProtectedUnPeekLiveData<List<VmsTerminalInfo>> getCacheVmsTerminalList() {
        return cacheVmsTerminalListLiveData;
    }

    public void addCacheTerminalList(List<VmsTerminalInfo> tempList) {
        if (tempList == null || tempList.size() == 0)
            return;

        List<VmsTerminalInfo> cacheList = cacheVmsTerminalListLiveData.getValue();
        if (cacheList == null)
            cacheList = new ArrayList<>();

        for (VmsTerminalInfo tempInfo : tempList) {
            boolean isExist = false;
            for (VmsTerminalInfo cacheInfo : cacheList) {
                if (cacheInfo.getSn().equals(tempInfo.getSn())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                cacheList.add(tempInfo);
            }
        }
        cacheVmsTerminalListLiveData.postValue(cacheList);
    }

    public void clearCacheTerminalList() {
        cacheVmsTerminalListLiveData.postValue(null);
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
