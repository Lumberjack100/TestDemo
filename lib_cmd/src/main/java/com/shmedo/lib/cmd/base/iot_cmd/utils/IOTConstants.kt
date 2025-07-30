package com.shmedo.lib.cmd.base.iot_cmd.utils

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

/**
 * IOT 相关常量
 */
object IOTConstants {
    /** 指令头 */
    const val COMMAND_HEADER = "\$cmd="

    /** 指令分隔符 */
    const val COMMAND_SPLICER = "&"

    /** 结果最小长度 */
    const val RESULT_MIN_LENGTH = 5

    /** 错误标志 */
    const val ERROR_FLAG = "result=fail"

    /** 空键值 */
    const val NULL_KEY = "NullKey"

    /**
     * 错误原因相关常量
     */
    object ErrorReasons {
        const val UNSUPPORTED = "unsupported"
        const val CMD_UNSUPPORTED = "cmd_unsupported"
        const val METHOD_UNSUPPORTED = "method-unsupported"
        const val STATE_NOT_READY = "state not ready"
        const val EQUIPMENT_MODEL_ERROR = "equimodel_err"
        const val CORS_NETWORK_ERROR = "PSRDIFF_ERROR_NET"
        const val CORS_NO_RESPONSE = "PSRDIFF_ERROR_CORS"
        const val GNSS_ERROR = "PSRDIFF_ERROR_GNSS"
        const val GNSS_SIGNAL_UNAVAILABLE = "PSRDIFF_ERROR_GPGGA"
        const val SWTOKEN = "swtoken"
    }

    /**
     * 错误原因中文映射
     */
    val errorReasonMap = mapOf(
        ErrorReasons.UNSUPPORTED to "设备不支持此功能",
        ErrorReasons.CMD_UNSUPPORTED to "设备不支持此功能",
        ErrorReasons.METHOD_UNSUPPORTED to "设备不支持此功能",
        ErrorReasons.STATE_NOT_READY to "状态未就绪",
        ErrorReasons.EQUIPMENT_MODEL_ERROR to "设备模式错误",
        ErrorReasons.CORS_NETWORK_ERROR to "网络错误",
        ErrorReasons.CORS_NO_RESPONSE to "CORS 服务无响应",
        ErrorReasons.GNSS_ERROR to "GNSS 模块异常",
        ErrorReasons.GNSS_SIGNAL_UNAVAILABLE to "GNSS 信号不可用",
        ErrorReasons.SWTOKEN to "水文标识重复，请修改后重新保存"
    )
}