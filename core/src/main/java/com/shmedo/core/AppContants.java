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
     * 设备类型
     */
    interface DeviceType {
        int UnKnown = 0x000;
        int DAS = 0x001;
        int ADME = 0x002;
        int VMS = 0x003;
        int E40 = 0x004;
        int M20 = 0x005;
        int RN20 = 0x006;
    }

    /**
     * 通讯方式
     */
    interface CommunicationWay {
        int NET_PLATFORM_CONNECT = 0x001;//通过物联网平台连接
        int BLE_CONNECT = 0x002;//通过蓝牙连接
        int TCP_CONNECT = 0x003;//通过本地TCP连接
    }

    /**
     * 数据中心配置方式
     */
    interface DataCenterConfigMethod {
        int BASIC_CONFIG = 0x001;//基本配置
        int ADVANCED_CONFIG = 0x002;//高级配置
    }

    interface Extras {
        String CUR_DEVICE_SN = "cur_device_sn";

        String COLLECTOR_MODE = "collector_mode";

        String SENSOR_TYPE = "sensor_type";

        String SENSOR_ADDRESS_LIST = "sensor_address_list";

        String SENSOR_ADDRESS = "sensor_address";

        String SENSOR_PARAM = "sensor_param";

        //地图 Poi 点信息
        String POIITEM_INFO = "poiitem_info";

        //地图 Poi 点坐标
        String POI_LATLNG = "poi_latlng";

        //地图 Poi 点名称
        String POI_TITLE = "poi_title";

        //与设备通讯方式
        String COMMUNICATION_WAY = "communication_way";

        //数据中心配置方式
        String DATA_CENTER_CONFIG_METHOD = "data_center_config_method";

        //数据中心编号
        String DATA_SERVER_NUMBER = "data_server_number";

        //数据中心状态
        String DATA_SERVER_STATUS = "data_server_status";

        // 设备类型
        String DEVICE_TYPE = "device_type";

        String DEVICE_ID = "device_id";
        String USB_PORT_NUM = "usb_port_num";
        String USB_BAUD_RATE = "usb_baud_rate";
    }

    interface UsbSerial {
        String INTENT_ACTION_GRANT_USB = "com.shmedo.mcloudapp.GRANT_USB";
        String INTENT_ACTION_DISCONNECT = "com.shmedo.mcloudapp.Disconnect";
    }

    interface MsgWhat {
        int MSG_DEFAULT = 0x001;//
        int CONNECT_DEVICE = 0x002;//

    }

}
