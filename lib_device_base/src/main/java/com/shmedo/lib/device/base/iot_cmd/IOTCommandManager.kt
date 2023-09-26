package com.shmedo.lib.device.base.iot_cmd

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/11/11 <br></br>
 * 描述：     TODO #gh#
 */
object IOTCommandManager {

    fun getCommand(commandType: IOTCommandType): String {
        return IOTCommand<Any>(commandType).toString()
    }

    fun <T> getCommand(commandType: IOTCommandType, parameter: T): String {
        return IOTCommand(commandType, parameter).toString()
    }

}