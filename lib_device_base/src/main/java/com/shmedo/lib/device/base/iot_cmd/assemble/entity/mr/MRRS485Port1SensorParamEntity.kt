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
    val sensoraddr: String = "",//传感器地址
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
    val paritybit: String = "",//校验位 1:NONE  2:ODD  3:EVEN  4:MARK 5:SPACE
    val stopbit: String = "",//停止位   1: 1  2: 1.5  3: 2  或 数字(1 1.5 2)
    val show: String = "",//展示指令信息（终端），1：展示，0：不展示
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
