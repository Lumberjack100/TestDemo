package com.shmedo.lib.device.base.iot_cmd

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/31 <br></br>
 * 描述：     TODO #gh#
 */
class IOTCommandResult<T> {
    /**
     * 命令执行是否成功
     */
    var isSuccess = false

    /**
     * 执行失败的错误信息
     */
    var message: String? = null

    /**
     * 命令类型
     */
    var commandType: IOTCommandType? = null

    /**
     * 解析结果数据
     */
    var result: T? = null


    companion object {
        const val COMMAND_HEADER = "\$cmd="
        const val RESULT_MIN_LENGTH = 5
        const val ERROR_FLAG = "result=fail"
    }
}