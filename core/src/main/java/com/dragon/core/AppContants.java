package com.dragon.core;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 文件名:   AppContants
 * 创建者:   dpc
 * 创建时间:  2019/1/24 11:45
 *
 */
public interface AppContants {
    String APP_CONFIG_NAME = "mcloudApp";
    String SERVICE_ADDRESS = "service_address";
    String TOKEN_UPDATE_TIME = "token_update_time";
    //是否显示隐私协议
    String PRIVACY_AGREEMENT = "privacy_agreement";

    String OSMOMETER_NOTE = "osmometer";
    String USER_HEAD_PHOTO_FILE_NAME = "/mnt/sdcard/tupian.png";


    interface User{
        String UID = "uid";
        String PWD = "pwd";
    }

    interface WiFi{
        String WIFI_STATE_CONNECT = "已连接";
        String WIFI_STATE_ON_CONNECTING = "正在连接";
        String WIFI_STATE_UNCONNECT = "未连接";
    }

}
