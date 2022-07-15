package com.shmedo.core;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 文件名:   AppContants
 * 创建者:   dpc
 * 创建时间:  2019/1/24 11:45
 */
public interface AppContants {
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
     * 通讯方式
     */
    interface CommunicationWay {
        int NET_PLATFORM_CONNECT = 0x001;//通过物联网平台连接
        int BLE_CONNECT = 0x002;//通过蓝牙连接
        int TCP_CONNECT = 0x003;//通过本地TCP连接
        int USB_SERIAL = 0x004;//通过 USB 串口连接
    }

    interface Extras {
        String CUR_DEVICE_SN = "cur_device_sn";

        String COLLECTOR_MODE = "collector_mode";

        String SENSOR_TYPE = "sensor_type";

        String SENSOR_ADDRESS_LIST = "sensor_address_list";

        String SENSOR_ADDRESS = "sensor_address";

        String SENSOR_PARAM = "sensor_param";

        //与设备通讯方式
        String COMMUNICATION_WAY = "communication_way";

        //数据中心配置方式
        String DATA_CENTER_CONFIG_METHOD = "data_center_config_method";

        //数据中心编号
        String DATA_SERVER_NUMBER = "data_server_number";

        //数据中心状态
        String DATA_SERVER_STATUS = "data_server_status";

        // 设备类型
        String PRODUCT_TYPE = "product_type";

        String DEVICE_ID = "device_id";

        String USB_PORT_NUM = "usb_port_num";

        String USB_BAUD_RATE = "usb_baud_rate";

        //蓝牙测斜仪测量间距
        String INCLINOMETER_MEASURINGSPACING = "inclinometer_measuringSpacing";
    }

    interface MsgWhat {
        int MSG_DEFAULT = 0x001;//
        int CONNECT_DEVICE = 0x002;//
        int MSG_HEART = 0x006;//
        int MSG_SMART_REFRESH = 0x007;//
    }

    enum UsbSerialMsgWhat implements INumberEnum {
        USB_SERIAL_DEVICE_INITIAL(1),
        USB_SERIAL_AT_SCAN(2),
        USB_SERIAL_AT_CONNECT(3),
        USB_SERIAL_LINK_QUERY(4),
        USB_SERIAL_COMMUNICATION_TIME(5),
        USB_SERIAL_OPEN_COMMUNICATION(6),
        USB_SERIAL_WORK_MODE(7),
        USB_SERIAL_DATA_QUERY(8),
        USB_SERIAL_COLLECTION_CONFIGURATION(9);

        private final int code;

        UsbSerialMsgWhat(int code) {
            this.code = code;
        }

        @Override
        public int getCode() {
            return code;
        }
    }


    /**
     * ADME 参数键值对键名
     */
    interface ADME {
        //上一次电机上拉速度
        String LAST_MOTOR_PULL_UP_SPEED = "last_motor_pull_up_speed";
        //上一次电机上拉距离
        String LAST_MOTOR_PULL_UP_DISTANCE = "last_motor_pull_up_distance";
        String LAST_MOTOR_DROP_SPEED = "last_motor_drop_speed";
        String LAST_MOTOR_DROP_DISTANCE = "last_motor_drop_distance";
    }

}
