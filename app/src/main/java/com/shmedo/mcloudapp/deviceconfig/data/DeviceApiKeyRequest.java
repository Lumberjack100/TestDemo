package com.shmedo.mcloudapp.deviceconfig.data;

import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.mcloudapp.deviceconfig.data.repository.DeviceRepository;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/4 <br/>
 * 描述：     TODO
 */
public class DeviceApiKeyRequest {
    private final UnPeekLiveData<String> deviceApiKey = new UnPeekLiveData.Builder<String>()
            .setAllowNullValue(true)
            .create();

    public UnPeekLiveData<String> getDeviceApiKeyLiveData() {

        return deviceApiKey;
    }

    public void queryDeviceApiKeyBySn(String sn) {
        DeviceRepository.getInstance().queryDeviceApiKeyBySn(sn, deviceApiKey);
    }

    public void clearDeviceApiKey() {
        if (deviceApiKey != null) {
            deviceApiKey.postValue(null);
        }
    }
}
