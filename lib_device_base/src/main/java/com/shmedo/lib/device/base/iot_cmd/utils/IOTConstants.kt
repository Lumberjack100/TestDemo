package com.shmedo.lib.device.base.iot_cmd.utils

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */
interface IOTConstants {

    companion object {
        const val COMMAND_HEADER = "\$cmd="
        const val RESULT_MIN_LENGTH = 5
        const val ERROR_FLAG = "result=fail"
        const val NULL_KEY = "NullKey"
    }
}