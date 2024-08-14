package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRS232Port2ParamEntity(
    var switch: String = "",//开关  1 开 0关
    var daddr: String = IOTConstants.NULL_KEY,//目的地址   数字
    var baud: String = IOTConstants.NULL_KEY,//波特率  bps 数字
    var databit: String = IOTConstants.NULL_KEY,//数据位   数字(5 6 7 8)
    var parity: String = IOTConstants.NULL_KEY,//校验位 0  NONE  1 ODD  2 EVEN  3 MARK 4 SPACE
    var stopbit: String = IOTConstants.NULL_KEY,//停止位   0: 1  1: 1.5  2: 2  或 数字(1 1.5 2)
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}