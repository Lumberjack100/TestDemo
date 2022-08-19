package com.shmedo.core

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 文件名:   AppContants
 * 创建者:   dpc
 * 创建时间:  2019/1/24 11:45
 */
interface AppContants {

    companion object {
        const val TOKEN_UPDATE_TIME = "token_update_time"

        //是否显示隐私协议
        const val PRIVACY_AGREEMENT = "privacy_agreement"
    }

    /**
     * 用户信息
     */
    interface User {
        companion object {
            const val UID = "uid"
            const val PWD = "pwd"
        }
    }

    /**
     * 通讯方式
     */
    interface CommunicationWay {
        companion object {
            const val NET_PLATFORM_CONNECT = 0x001 //通过物联网平台连接
            const val BLE_CONNECT = 0x002 //通过蓝牙连接
            const val TCP_CONNECT = 0x003 //通过本地TCP连接
            const val USB_SERIAL = 0x004 //通过 USB 串口连接
        }
    }

    interface Extras {
        companion object {
            //与设备通讯方式
            const val COMMUNICATION_WAY = "communication_way"

            //产品类型
            const val PRODUCT_TYPE = "product_type"

            //设备信息
            const val DEVICE_INFO = "device_info"
            const val DEVICE_SN = "device_sn"
            const val COLLECTOR_MODE = "collector_mode"
            const val SENSOR_TYPE = "sensor_type"
            const val SENSOR_ADDRESS_LIST = "sensor_address_list"
            const val SENSOR_ADDRESS = "sensor_address"
            const val SENSOR_PARAM = "sensor_param"

            //数据中心配置方式
            const val DATA_CENTER_CONFIG_METHOD = "data_center_config_method"

            //数据中心编号
            const val DATA_CENTER_NUMBER = "data_center_number"

            //数据中心状态
            const val DATA_CENTER_STATUS = "data_center_status"
            const val USB_PORT_NUM = "usb_port_num"
            const val USB_BAUD_RATE = "usb_baud_rate"
            const val USB_DEVICE_ID = "usb_device_id"

            //ADME 电机运动信息
            const val MOTOR_INFO = "motor_info"

            //蓝牙测斜仪测量间距
            const val INCLINOMETER_MEASURINGSPACING = "inclinometer_measuringSpacing"
        }
    }

    interface MsgWhat {
        companion object {
            const val MSG_DEFAULT = 0x001 //
            const val CONNECT_DEVICE = 0x002 //
            const val MSG_HEART = 0x006 //
            const val MSG_SMART_REFRESH = 0x007 //
            const val MSG_POLLING = 0x003
        }
    }

    enum class UsbSerialMsgWhat(private val code: Int) : INumberEnum {
        USB_SERIAL_DEVICE_INITIAL(1),
        USB_SERIAL_AT_SCAN(2),
        USB_SERIAL_AT_CONNECT(3),
        USB_SERIAL_LINK_QUERY(4),
        USB_SERIAL_COMMUNICATION_TIME(5),
        USB_SERIAL_OPEN_COMMUNICATION(6),
        USB_SERIAL_WORK_MODE(7),
        USB_SERIAL_DATA_QUERY(8),
        USB_SERIAL_COLLECTION_CONFIGURATION(9);

        override fun getCode(): Int {
            return code
        }
    }

    /**
     * ADME 参数键值对键名
     */
    interface ADME {
        companion object {
            /**手动测孔深本地存储键值对*/
            const val MANUAL_LAST_MOTOR_PULL_UP_SPEED = "last_motor_pull_up_speed" //上一次电机上拉速度
            const val MANUAL_LAST_MOTOR_PULL_UP_DISTANCE = "last_motor_pull_up_distance" //上一次电机上拉距离
            const val MANUAL_LAST_MOTOR_DROP_SPEED = "last_motor_drop_speed"
            const val MANUAL_LAST_MOTOR_DROP_DISTANCE = "last_motor_drop_distance"

            /**自动测孔深本地存储键值对*/
            const val AUTO_LAST_MOTOR_DROP_SPEED = "auto_last_motor_drop_speed"//上一次电机下放速度
            const val AUTO_LAST_BOTTOM_SAFE_DISTANCE = "auto_last_bottom_safe_distance"//上一次管底安全补偿距离
        }
    }

}