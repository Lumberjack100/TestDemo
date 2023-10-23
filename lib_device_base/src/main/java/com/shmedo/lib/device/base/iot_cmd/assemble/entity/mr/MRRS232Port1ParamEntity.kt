package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRS232Port1ParamEntity(
    val switch: String = "",//开关  1 开 0关
    val type: String = IOTConstants.NULL_KEY,//摄像头类型   1: 类型1  2: 类型2 3: 类型3
    val resolut: String = IOTConstants.NULL_KEY,//分辨率  1 分辨率1  2 分辨率2 3 分辨率3
    val interval: String = IOTConstants.NULL_KEY,//拍照间隔  s  数字
    val baud: String = IOTConstants.NULL_KEY,//波特率  bps 数字
    val databit: String = IOTConstants.NULL_KEY,//数据位   数字(5 6 7 8)
    val paritybit : String = IOTConstants.NULL_KEY,//校验位 1  NONE  2 ODD  3 EVEN  4 MARK 5 SPACE
    val stopbit  : String = IOTConstants.NULL_KEY,//停止位   1: 1  2: 1.5  3: 2  或 数字(1 1.5 2)
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
