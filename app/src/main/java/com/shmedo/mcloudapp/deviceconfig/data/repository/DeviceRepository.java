package com.shmedo.mcloudapp.deviceconfig.data.repository;

import android.text.TextUtils;

import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.deviceconfig.model.DetailDeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceBaseInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceSimpleInfo;
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
    public void queryDeviceApiKeyBySn(String sn, UnPeekLiveData<DeviceBaseInfo> deviceApiKey) {
        GetDeviceDetailInfo(sn, deviceApiKey);
//        String apiKey = DeviceDao.getInstance().getCachedDeviceApiKeyBySn(sn);
//        if (apiKey == null) {
//            queryCompanyDevice(sn);
//        } else {
//            deviceApiKey.postValue(apiKey);
//        }
    }

    /**
     * 获取设备详细信息
     */
    private void GetDeviceDetailInfo(String deviceToken, UnPeekLiveData<DeviceBaseInfo> deviceApiKey) {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("deviceToken", deviceToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS)
                .GetDeviceDetail(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<DetailDeviceInfo>() {
                    @Override
                    protected void onResponse(DetailDeviceInfo detailDeviceInfo, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (detailDeviceInfo == null || detailDeviceInfo.getDeviceBaseInfo() == null || TextUtils.isEmpty(detailDeviceInfo.getDeviceBaseInfo().getApikey())) {
                                    deviceApiKey.postValue(null);
                                    return;
                                }
                                MCloudApp.setProductID(detailDeviceInfo.getDeviceBaseInfo().getProductID());
                                deviceApiKey.postValue(detailDeviceInfo.getDeviceBaseInfo());
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

    /**
     * 获取设备ProductToken
     */
    public void queryProductTokenBySn(String deviceToken, UnPeekLiveData<String> productToken) {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("deviceToken", deviceToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS)
                .GetDeviceSimpleInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<DeviceSimpleInfo>() {
                    @Override
                    protected void onResponse(DeviceSimpleInfo deviceSimpleInfo, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (deviceSimpleInfo == null || TextUtils.isEmpty(deviceSimpleInfo.getProductToken())) {
                                    productToken.postValue(null);
                                    return;
                                }
                                productToken.postValue(deviceSimpleInfo.getProductToken());
                            } else {
                                productToken.postValue(null);
                            }
                        } else {
                            productToken.postValue(null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
//                        ResponseHandler.getInstance().handleFailure((Exception) e);
                        productToken.postValue(null);
                    }
                });
    }
}
