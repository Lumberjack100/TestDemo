package com.shmedo.mcloudapp.network;

import com.shmedo.core.MCloudApp;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model
 * 文件名:   BaseRetrofit
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:29
 * 描述：    Retrofit 基类
 */
public abstract class BaseRetrofit {

    protected <T> T getService(final Class<T> service, final HttpLoggingInterceptor.Level level) {
        return this.getService(service, level, ServiceAddressType.AUTHORITY_SERVICE_ADDRESS);
    }

    protected <T> T getService(final Class<T> service, final HttpLoggingInterceptor.Level level, ServiceAddressType addressType) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(level);
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addNetworkInterceptor(interceptor)
                .build();

        String baseUrl = "";
        switch (addressType) {
            case AUTHORITY_SERVICE_ADDRESS://权限服务地址
                baseUrl = MCloudApp.getAuthorityServiceAddress();
                break;

            case IOT_MANAGER_SERVICE_ADDRESS://物联网设备管理服务地址
                baseUrl = MCloudApp.getIotManagerServiceAddress();
                break;

            case IOT_INTERACTIVE_SERVICE_ADDRESS://物联网指令交互服务地址
                baseUrl = MCloudApp.getIotInteractiveServiceAddress();
                break;

            case CLOUD_PLATFORM_DATA_ADDRESS://云平台原始数据地址
                baseUrl = MCloudApp.getCloudPlatformDataAddress();
                break;

            case DEVICE_REMOTE_DEBUG_ADDRESS:
                baseUrl = MCloudApp.getDeviceRemoteDebugAddress();
                break;

            default:
                break;
        }
        return new Retrofit.Builder()
                //设置网络请求的Url地址
                .baseUrl(baseUrl)
                .client(client)
                //设置数据解析器
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(service);
    }
}
