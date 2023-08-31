package com.shmedo.lib.device.base.iot_cmd.entity

import com.shmedo.lib.device.base.Validater
import com.shmedo.lib.device.base.exception.DASParameterException

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/16/20 <br></br>
 * 描述：     获取服务器(数据中心)编号参数
 */
class ServerNumberEntity(private val number: Int) : Validater {
    override fun validate() {
        if (number != 1 && number != 2 && number != 3 && number != 4) throw DASParameterException("服务器编号不存在")
    }

    override fun toString(): String {
        return "centerid=$number"
    }
}