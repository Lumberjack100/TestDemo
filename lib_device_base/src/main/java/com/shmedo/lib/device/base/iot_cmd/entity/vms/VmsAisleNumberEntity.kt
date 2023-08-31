package com.shmedo.lib.device.base.iot_cmd.entity.vms

import com.shmedo.lib.device.base.Validater
import com.shmedo.lib.device.base.exception.DASParameterException

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/11/13 <br></br>
 * 描述：      Vms网关通道编号参数
 */
class VmsAisleNumberEntity(private val number: Int) : Validater {
    override fun validate() {
        if (number != 0 && number != 1 && number != 2) throw DASParameterException("Vms网关通道编号不存在")
    }

    override fun toString(): String {
        return "channel=$number"
    }
}