package com.shmedo.mcloudapp.model;

import android.util.Log;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model
 * 文件名:   BaseRetrofit
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:29
 * 描述：    Retrofit 基类
 */
public class BaseRetrofit {

    public <T> T  getService(final Class<T> service, final HttpLoggingInterceptor.Level level){

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(level);
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .retryOnConnectionFailure(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .addNetworkInterceptor(interceptor)
            .build();

        Log.i("adu","=======fuwuqidizhi====="+CommonVariable.getServiceAddress());
        return new Retrofit.Builder()
            //设置网络请求的Url地址
            .baseUrl(CommonVariable.getServiceAddress())
            .client(client)
            //设置数据解析器
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(service);


    }

}
