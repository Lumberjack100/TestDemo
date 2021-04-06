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
    private UnPeekLiveData<Boolean> vmsRefreshTerminal;
    private List<VmsTerminalInfo> cacheVmsTerminalList;

    public List<VmsTerminalInfo> getCacheVmsTerminalList() {
        return cacheVmsTerminalList;
    }

    /**
     * 添加 Vms 挂载的终端设备到缓存列表中
     *
     * @param tempList
     */
    public void addCacheTerminalList(List<VmsTerminalInfo> tempList) {
        if (tempList == null || tempList.size() == 0)
            return;

        if (cacheVmsTerminalList == null)
            cacheVmsTerminalList = new ArrayList<>();

        for (VmsTerminalInfo tempInfo : tempList) {
            boolean isExist = false;
            for (VmsTerminalInfo cacheInfo : cacheVmsTerminalList) {
                if (cacheInfo.getSn().equals(tempInfo.getSn())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                cacheVmsTerminalList.add(tempInfo);
            }
        }
    }

    public void removeTerminal(String sn) {
        VmsTerminalInfo cacheInfo = null;
        for (VmsTerminalInfo terminalInfo : cacheVmsTerminalList) {
            if (terminalInfo.getSn().equals(sn)) {
                cacheInfo = terminalInfo;
                break;
            }
        }
        if (cacheInfo != null)
            cacheVmsTerminalList.remove(cacheInfo);
    }

    public void clearCacheTerminalList() {
        if (cacheVmsTerminalList != null)
            cacheVmsTerminalList.clear();
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
