package com.shmedo.mcloudapp.model;

import com.shmedo.mcloudapp.model.api.ApiService;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model
 * 文件名:   MDRetrofit
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:40
 *
 */
public class MDRetrofit extends BaseRetrofit {


    private static MDRetrofit instance;


    static {
        instance = new MDRetrofit();
    }

    public static MDRetrofit getInstance() {
        return instance;
    }

    public <T> T getService(Class<T> service) {
        return getService(service, HttpLoggingInterceptor.Level.BODY);
    }

    public ApiService createService() {
        return getService(ApiService.class);
    }

}

