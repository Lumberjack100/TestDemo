package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/14
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class AdmeStepperMotorEntity(
    val posnegtest: String = "", //正反测（0:关闭，1:开启）
    val absprsion: String = IOTConstants.NULL_KEY, //绝对精度修正值
    val movspeed: String = IOTConstants.NULL_KEY, //步进电机运动速度
    val movesm: String = IOTConstants.NULL_KEY, //步进电机力矩
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
