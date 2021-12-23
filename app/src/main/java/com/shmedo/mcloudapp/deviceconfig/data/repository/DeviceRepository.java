package com.shmedo.mcloudapp.deviceconfig.data.repository;

import com.blankj.utilcode.util.GsonUtils;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.params.QueryProjectDevice;
import com.shmedo.mcloudapp.entity.DeviceDetailInfo;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.network.ServiceAddressType;
import com.shmedo.mcloudapp.util.ResponseHandler;

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
        queryCompanyDevice(sn, deviceApiKey);
//        String apiKey = DeviceDao.getInstance().getCachedDeviceApiKeyBySn(sn);
//        if (apiKey == null) {
//            queryCompanyDevice(sn);
//        } else {
//            deviceApiKey.postValue(apiKey);
//        }
    }


    /**
     * 查询公司设备列表
     */
    private void queryCompanyDevice(String sn, UnPeekLiveData<String> deviceApiKey) {
        QueryProjectDevice parameter = new QueryProjectDevice();
        parameter.setCompanyID(MCloudApp.getCompanyID());
        parameter.setDeviceType(-1);
//        parameter.setDeviceStatus("启用");
        parameter.setPageSize(10);
        parameter.setCurrentPage(1);
        parameter.setSn(sn);

        String json = GsonUtils.toJson(parameter);
        RequestBody body = RequestBody.create(RequestHeader.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS)
                .getDeviceList(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PageResult<DeviceInfo>>() {
                    @Override
                    protected void onResponse(PageResult<DeviceInfo> data, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    deviceApiKey.postValue(null);
                                    return;
                                }
                                GetDeviceDetailInfo(data.getCurrentPageData().get(0).getId(), deviceApiKey);

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

    private void GetDeviceDetailInfo(int deviceId, UnPeekLiveData<String> deviceApiKey) {
        RequestBody body = RequestBody.create(RequestHeader.JSON_TYPE, String.valueOf(deviceId));

        MDRetrofit.getInstance()
                .createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS)
                .getDescribeDeviceSimpleInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<DeviceDetailInfo>() {
                    @Override
                    protected void onResponse(DeviceDetailInfo deviceDetailInfo, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (deviceDetailInfo == null || deviceDetailInfo.getBasicInfo() == null) {
                                    deviceApiKey.postValue(null);
                                    return;
                                }
                                deviceApiKey.postValue(deviceDetailInfo.getBasicInfo().getApiKey());

//                                DeviceApiKey apiKey = new DeviceApiKey();
//                                apiKey.setApiKey(deviceDetailInfo.getBasicInfo().getApiKey());
//                                apiKey.setDeviceID(deviceDetailInfo.getBasicInfo().getDeviceID());
//                                apiKey.setDeviceToken(deviceDetailInfo.getBasicInfo().getDeviceToken());
//                                DeviceDao.getInstance().cacheDeviceApiKey(apiKey);
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
