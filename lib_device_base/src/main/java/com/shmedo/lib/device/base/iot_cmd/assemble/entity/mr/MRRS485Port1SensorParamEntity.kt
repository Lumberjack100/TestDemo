package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/27 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRS485Port1SensorParamEntity(
    val model: String = "",//物模型
    val c_model: String = "",//创建新指令    --创建新指令发1。修改指令发0
    val num: String = "",//物模型变量	--创建新指令时可以传任意值。修改指令根据传指定变量
    val swtoken: String = "",//水文标识
    val cmd: String = "",//传感器采集指令
    val ratio: String = "",//倍率
    val dataformat: String = "",//数据类型
    val calctype: String = "",//解算方式   --目前只支持 0:加权平均
    val gateval: String = "",//触发值
    val uplimit: String = "",//上限值
    val lowlimit: String = "",//下限值
    val corrvalue: String = "",//修正值
    val baud: String = "",//波特率  bps 数字
    val databit: String = "",//数据位   数字(5 6 7 8)
    val parity: String = "",//校验位 0:NONE  1:ODD  2:EVEN  3:MARK 4:SPACE
    val stopbit: String = "",//停止位   0: 1  1: 1.5  2: 2  或 数字(1 1.5 2)
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
