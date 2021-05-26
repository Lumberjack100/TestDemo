package com.shmedo.mcloudapp.deviceconfig.data.repository;

import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.deviceconfig.data.db.DeviceDao;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceApiKey;
import com.shmedo.mcloudapp.entity.DeviceDetailInfo;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.projects.model.param.QueryProjectDevice;
import com.shmedo.mcloudapp.util.ResponseHandler;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/25/20 <br/>
 * 描述：     TODO #gh#
 */
public class DeviceRepository {
    private static final DeviceRepository instance = new DeviceRepository();
    private UnPeekLiveData<String> deviceApiKey = new UnPeekLiveData.Builder<String>()
            .setAllowNullValue(true)
            .create();

    public static DeviceRepository getInstance() {
        return instance;
    }

    public UnPeekLiveData<String> getDeviceApiKeyLiveData() {
        if (deviceApiKey == null) {
            deviceApiKey = new UnPeekLiveData.Builder<String>()
                    .setAllowNullValue(true)
                    .create();
        }

        return deviceApiKey;
    }

    public void clearDeviceApiKey() {
        if (deviceApiKey != null) {
            deviceApiKey.postValue(null);
        }
    }

    /**
     * 根据设备 SN号 返回设备的 apiKey
     *
     * @param sn
     */
    public void queryDeviceApiKeyBySn(String sn) {
        queryCompanyDevice(sn);
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
    private void queryCompanyDevice(String sn) {
        QueryProjectDevice parameter = new QueryProjectDevice();
        parameter.setCompanyID(MCloudApp.getCompanyID());
        parameter.setDeviceType(-1);
//        parameter.setDeviceStatus("启用");
        parameter.setPageSize(10);
        parameter.setCurrentPage(1);
        parameter.setSn(sn);

        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryCompanyDevice(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PageResult<ProjectDeviceInfo>>() {
                    @Override
                    protected void onResponse(PageResult<ProjectDeviceInfo> data, ErrCode errCode) {
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    deviceApiKey.postValue(null);
                                    return;
                                }
                                GetDeviceDetailInfo(data.getCurrentPageData().get(0).getId());

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

    private void GetDeviceDetailInfo(int deviceId) {
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, String.valueOf(deviceId));

        MDRetrofit.getInstance()
                .createService()
                .GetDeviceDetailInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<DeviceDetailInfo>() {
                    @Override
                    protected void onResponse(DeviceDetailInfo deviceDetailInfo, ErrCode errCode) {
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (deviceDetailInfo == null || deviceDetailInfo.getBasicInfo() == null) {
                                    deviceApiKey.postValue(null);
                                    return;
                                }
                                deviceApiKey.postValue(deviceDetailInfo.getBasicInfo().getApiKey());

                                DeviceApiKey apiKey = new DeviceApiKey();
                                apiKey.setApiKey(deviceDetailInfo.getBasicInfo().getApiKey());
                                apiKey.setDeviceID(deviceDetailInfo.getBasicInfo().getDeviceID());
                                apiKey.setDeviceToken(deviceDetailInfo.getBasicInfo().getDeviceToken());
                                DeviceDao.getInstance().cacheDeviceApiKey(apiKey);
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
