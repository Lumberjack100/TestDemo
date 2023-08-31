package com.shmedo.lib.device.base.iot_cmd.entity.e40

import com.shmedo.lib.device.base.Validater
import com.shmedo.lib.device.base.exception.DASParameterException

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/26 <br></br>
 * 描述：      查询卫星信息
 */
class SatelitteTypeEntity(private val type: String) : Validater {
    override fun validate() {
        if (type != "ALL" && type != "BDS" && type != "GPS" && type != "GLO") throw DASParameterException(
            "卫星类别不存在"
        )
    }

    override fun toString(): String {
        return "type=$type"
    }
}