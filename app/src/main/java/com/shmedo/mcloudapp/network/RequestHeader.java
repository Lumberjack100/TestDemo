package com.shmedo.mcloudapp.network;

import okhttp3.MediaType;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model.common
 * 文件名:   CommonVariable
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:34
 */
public interface RequestHeader {
    MediaType JSON_TYPE = MediaType.parse("application/json; charset=UTF-8");
    String HEADER_ACCESS_TYPE = "access_type:android";
    String ACCESS_TOKEN = "Authorization";
    String ACCESS_SERVICE = "access_service:mcloud";

}
