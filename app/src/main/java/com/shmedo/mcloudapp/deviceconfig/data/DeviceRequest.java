package com.shmedo.mcloudapp.deviceconfig.data;

import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.mcloudapp.deviceconfig.data.repository.DeviceRepository;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceBaseInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/4 <br/>
 * 描述：     TODO
 */
public class DeviceRequest {
    private final UnPeekLiveData<DeviceBaseInfo> deviceApiKey = new UnPeekLiveData.Builder<DeviceBaseInfo>()
            .setAllowNullValue(true)
            .create();

    private final UnPeekLiveData<String> productToken = new UnPeekLiveData.Builder<String>()
            .setAllowNullValue(true)
            .create();

    public UnPeekLiveData<DeviceBaseInfo> getDeviceApiKeyLiveData() {
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

    public UnPeekLiveData<String> getProductTokenLiveData() {
        return productToken;
    }

    public void queryProductTokenBySn(String sn) {
        DeviceRepository.getInstance().queryProductTokenBySn(sn, productToken);
    }

    public void clearProductToken() {
        if (productToken != null) {
            productToken.postValue(null);
        }
    }
}
