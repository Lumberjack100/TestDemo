package com.shmedo.mcloudapp.network;

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

        String baseUrl = addressType.getBaseUrl();
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
