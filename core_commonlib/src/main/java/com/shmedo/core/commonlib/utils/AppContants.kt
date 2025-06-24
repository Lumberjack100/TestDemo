package com.shmedo.core.commonlib.utils

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/2/13 <br/>
 * 描述：     TODO
 */
interface AppContants {

    companion object {
        const val PLACE_HOLDER_VALUE = "- -"
    }

    interface Extras {
        companion object {
            //状态栏颜色
            const val STATUS_BAR_COLOR = "status_bar_color"
            const val IS_REFRESH_USER_INFO = "is_refresh_user_info"
            const val DEVICE_SEARCH_KEYWORD = "device_search_keyword"

            //与设备通讯方式
            const val COMMUNICATION_WAY = "communication_way"

            //产品类型
            const val PRODUCT_TYPE = "product_type"

            //4G 设备信息
            const val DEVICE_INFO = "device_info"

            //蓝牙设备信息
            const val BLE_DEVICE = "ble_device"

            const val SERVER_NUMBER = "server_number"

            const val SENSOR_CHANNEL = "sensor_channel"

            const val SENSOR_INDEX = "sensor_index"

            const val SENSOR_ADDR = "sensor_addr"

            const val FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY =
                "fragment_data_center_home_result_request_key"

            const val REFRESH_DATA_CENTER_STATUS = "refresh_data_center_status"

            const val FRAGMENT_MEASURING_DATA_PROCEDURE_RESULT_REQUEST_KEY =
                "fragment_measuring_data_procedure_result_request_key"

            //ADME 电机运动 状态信息
            const val MOTOR_STATE = "motor_state"
        }
    }

    /**
     * 通讯方式
     */
    interface Communication {
        companion object {
            const val DELAY_5000_MILLIS = 5000L
            const val DELAY_10000_MILLIS = 10000L //发送指令超时时间
            const val DELAY_15000_MILLIS = 15000L //蓝牙连接超时时间
            const val DELAY_20000_MILLIS = 20000L
            const val DELAY_25000_MILLIS = 25000L
            const val DELAY_30000_MILLIS = 30000L
            const val DELAY_40000_MILLIS = 40000L
            const val DELAY_60000_MILLIS = 60000L

            const val DELAY_CHECK_DEVICE_ONLINE_STATUS = 60000L // 检查设备物联网平台在线状态间隔
            const val DELAY_BLE_HEART_BEAT = 20000L // 蓝牙连接心跳包发送间隔
        }
    }
}