package com.shmedo.mcloudapp.deviceconfig.data.repository;

import android.text.TextUtils;

import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.deviceconfig.model.BasicDeviceInfo;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.network.ServiceAddressType;
import com.shmedo.mcloudapp.util.ResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/25/20 <br/>
 * 描述：     TODO #gh#
 */
public class DeviceRepository {
    private static final DeviceRepository instance = new DeviceRepository();

    public static DeviceRepository getInstance() {
        return instance;
    }

    /**
     * 根据设备 SN号 返回设备的 apiKey
     *
     * @param sn
     */
    public void queryDeviceApiKeyBySn(String sn, UnPeekLiveData<String> deviceApiKey) {
        GetDeviceSimpleInfo(sn, deviceApiKey);
//        String apiKey = DeviceDao.getInstance().getCachedDeviceApiKeyBySn(sn);
//        if (apiKey == null) {
//            queryCompanyDevice(sn);
//        } else {
//            deviceApiKey.postValue(apiKey);
//        }
    }

    /**
     * 获取设备概要信息
     */
    private void GetDeviceSimpleInfo(String deviceToken, UnPeekLiveData<String> deviceApiKey) {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("deviceToken", deviceToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS)
                .getDescribeDeviceSimpleInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BasicDeviceInfo>() {
                    @Override
                    protected void onResponse(BasicDeviceInfo basicDeviceInfo, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (basicDeviceInfo == null || TextUtils.isEmpty(basicDeviceInfo.getApiKey())) {
                                    deviceApiKey.postValue(null);
                                    return;
                                }
                                MCloudApp.setProductID(basicDeviceInfo.getProductID());
                                deviceApiKey.postValue(basicDeviceInfo.getApiKey());
                            } else {
                                deviceApiKey.postValue(null);
                            }
                        } else {
                            deviceApiKey.postValue(null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
//                        ResponseHandler.getInstance().handleFailure((Exception) e);
                        deviceApiKey.postValue(null);
                    }
                });
    }
}
