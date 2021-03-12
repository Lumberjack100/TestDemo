package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;

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

    public ProtectedUnPeekLiveData<List<VmsTerminalInfo>> getCacheVmsTerminalList() {
        return cacheVmsTerminalListLiveData;
    }

    /**
     * 添加 Vms 挂载的终端设备到缓存列表中
     *
     * @param tempList
     */
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
        List<VmsTerminalInfo> cacheList = cacheVmsTerminalListLiveData.getValue();
        if (cacheList != null)
            cacheList.clear();
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
