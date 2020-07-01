package com.shmedo.mcloudapp.interfaces;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model
 * 创建者:   gonghe
 * 创建时间:  2019-09-10
 *
 */
public interface Extras {

    String QUERY_PROJECT_DEVICE = "queryProjectDevice";

    String CUR_DEVICE_NAME = "cur_device_name";

    String DEVICE_MAC_ADDRESS = "cur_device_mac_address";

    String DEVICE_E60 = "device_e60";

    String PARAM_CONFIG_INFO = "param_config_info";

    String DEBUG_MODE = "debug_mode";

    String COLLECTOR_MODE = "collector_mode";

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

}
