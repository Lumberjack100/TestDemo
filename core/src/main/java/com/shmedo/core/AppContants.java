package com.shmedo.core;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 文件名:   AppContants
 * 创建者:   dpc
 * 创建时间:  2019/1/24 11:45
 */
public interface AppContants {
    String SERVICE_ADDRESS = "service_address";
    String TOKEN_UPDATE_TIME = "token_update_time";
    //是否显示隐私协议
    String PRIVACY_AGREEMENT = "privacy_agreement";

    /**
     * 用户信息
     */
    interface User {
        String UID = "uid";
        String PWD = "pwd";
    }

    /**
     * WiFi
     */
    interface WiFi {
        String WIFI_STATE_CONNECT = "已连接";
        String WIFI_STATE_ON_CONNECTING = "正在连接";
        String WIFI_STATE_UNCONNECT = "未连接";
    }

    /**
     * 通讯方式
     */
    interface CommunicationWay {
        int NET_PLATFORM_CONNECT = 0x001;//通过物联网平台连接
        int BLE_CONNECT = 0x002;//通过蓝牙连接
        int TCP_CONNECT = 0x003;//通过本地TCP连接
    }


    interface Extras {

        String CUR_DEVICE_SN = "cur_device_sn";

        String CUR_DEVICE_NAME = "cur_device_name";

        String CUR_BLE_DEVICE_INFO = "cur_ble_device_info";

        String PARAM_CONFIG_INFO = "param_config_info";

        String DEBUG_MODE = "debug_mode";

        String COLLECTOR_MODE = "collector_mode";

        String SENSOR_TYPE = "sensor_type";

        String SENSOR_ADDRESS = "sensor_address";

        String SENSOR_PARAM = "sensor_param";

        String SPLICE_SENSOR_PARAMS = "splice_sensor_params";

        //ADME的 DAG 采集器配置
        String ADME_SENSOR_CONFIG_INFO = "adme_sensor_config_info";

        //ADME的执行机构配置
        String ADME_EXECUTIVE_AGENCY_CONFIG_INFO = "adme_executive_agency_config_info";

        //ADME的控制电机配置
        String ADME_MOTOR_CONTROL_CONFIG_INFO = "adme_motor_control_config_info";

        //ADME的计米轮参数配置
        String ADME_COUNT_METER_WHEEL_CONFIG_INFO = "adme_count_meter_wheel_config_info";

        //编码器修正参数
        String ENCODER_CORRECTION_PARAMETERS = "encoder_correction_parameters";

        //地图 Poi 点信息
        String POIITEM_INFO = "poiitem_info";

        //地图 Poi 点坐标
        String POI_LATLNG = "poi_latlng";

        //地图 Poi 点名称
        String POI_TITLE = "poi_title";

        //与设备通讯方式
        String COMMUNICATION_WAY = "communication_way";
    }

}
