package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/20 <br/>
 * 描述：     RS485-3-摄像头参数实体
 */
@JsonClass(generateAdapter = true)
data class MRRS485Port3CameraParamEntity(
    val index: String = IOTConstants.NULL_KEY,
    val switch: String = IOTConstants.NULL_KEY,
    val addr: String = IOTConstants.NULL_KEY,
    val baud: String = IOTConstants.NULL_KEY,
    val databit: String = IOTConstants.NULL_KEY,
    val parity: String = IOTConstants.NULL_KEY,
    val stopbit: String = IOTConstants.NULL_KEY,
    val type: String = IOTConstants.NULL_KEY,
    val resolut: String = IOTConstants.NULL_KEY,
    val quality: String = IOTConstants.NULL_KEY,
    val interval: String = IOTConstants.NULL_KEY,
    val workmode: String = IOTConstants.NULL_KEY
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
} 