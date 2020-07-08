package com.shmedo.mcloudapp.network;

import okhttp3.MediaType;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model.common
 * 文件名:   CommonVariable
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:34
 */
public interface NetworkConst {

    MediaType JSON_TYPE = MediaType.parse("application/json; charset=UTF-8");
    String HEADER_ACCESS_TYPE = "access_type:android";
    String HEADER_APP_KEY = "app_key:b4524704-b325-4c88-a0dc-bfce89d58138";
    String HEADER_APP_SECRET = "app_secret:c1507673-7a16-4cba-817c-2595a9c8a6e8";
    String ACCESS_TOKEN = "access_token";

}
