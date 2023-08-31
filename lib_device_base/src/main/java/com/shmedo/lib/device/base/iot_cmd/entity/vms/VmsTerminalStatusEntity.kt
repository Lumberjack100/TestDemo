package com.shmedo.lib.device.base.iot_cmd.entity.vms

import com.shmedo.lib.device.base.Validater
import com.shmedo.lib.device.base.exception.DASParameterException

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/23/21 <br></br>
 * 描述：    Vms 挂载终端的运行情况
 */
class VmsTerminalStatusEntity(private val channelNumber: Int, private val index: Int) : Validater {
    override fun validate() {
        if (channelNumber != 0 && channelNumber != 1 && channelNumber != 2) throw DASParameterException("Vms网关通道编号不存在")
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder().apply {
            append("channel=$channelNumber")
            append("&")
            append("index=$index")
        }

        return stringBuilder.toString()
    }
}