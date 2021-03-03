package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.shmedo.mcloudapp.deviceconfig.data.repository.DeviceRepository;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/3/21 <br/>
 * 描述：     TODO #gh#
 */
public class DeviceApiKeyViewModel extends ViewModel {
    public ProtectedUnPeekLiveData<String> getDeviceApiKey() {
        return DeviceRepository.getInstance().getDeviceApiKeyLiveData();
    }

    public void clearDeviceApiKey() {
        DeviceRepository.getInstance().clearDeviceApiKey();
    }

    public void queryDeviceApiKeyBySn(String sn) {
        DeviceRepository.getInstance().queryDeviceApiKeyBySn(sn);
    }
}
