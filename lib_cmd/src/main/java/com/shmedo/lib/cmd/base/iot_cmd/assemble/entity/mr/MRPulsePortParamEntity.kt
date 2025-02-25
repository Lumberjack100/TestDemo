package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     脉冲端口参数实体
 */
@JsonClass(generateAdapter = true)
data class MRPulsePortParamEntity(
    val switch: String = "",//开关  1 开 0关
    val workmode: String = IOTConstants.NULL_KEY,//功能模式 计数 消警
    val dryaccuracy: String = IOTConstants.NULL_KEY,//脉冲分辨率 默认1，整型，大于0，最大9999
    val dryelim: String = IOTConstants.NULL_KEY,//消抖系数 默认5，整型，大于0，最大60.单位s
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
} 