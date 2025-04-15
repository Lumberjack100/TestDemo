package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 报警模块参数设置实体类
 */
@JsonClass(generateAdapter = true)
data class MRAlarmModuleParamEntity(
    val index: String = "",                   // 类型索引号 0：雨量 1：水位
    val switch: String = IOTConstants.NULL_KEY,       // 功能开关 开：1 关：0
    val relay: String = IOTConstants.NULL_KEY,        // 继电器K接口控制 开：1 关：0
    val warnlevel: String = IOTConstants.NULL_KEY,    // 报警阈值 雨量单位：mm，水位单位：m
    val holdtime1: String = IOTConstants.NULL_KEY,     // 一级报警报警持续时间 数值≥1，单位：分钟
    val holdtime2: String = IOTConstants.NULL_KEY,     // 二级报警报警持续时间 数值≥1，单位：分钟
    val holdtime3: String = IOTConstants.NULL_KEY,     // 三级报警报警持续时间 数值≥1，单位：分钟
    val holdtime4: String = IOTConstants.NULL_KEY,     // 四级报警报警持续时间 数值≥1，单位：分钟
    val gaptime1: String = IOTConstants.NULL_KEY,      // 一级报警间隔时间 数值≥1，单位：分钟
    val gaptime2: String = IOTConstants.NULL_KEY,      // 二级报警间隔时间 数值≥1，单位：分钟
    val gaptime3: String = IOTConstants.NULL_KEY,      // 三级报警间隔时间 数值≥1，单位：分钟
    val gaptime4: String = IOTConstants.NULL_KEY,      // 四级报警间隔时间 数值≥1，单位：分钟
    val voiceindex1: String = IOTConstants.NULL_KEY,  // 等级1语音报警发送指令
    val voiceindex2: String = IOTConstants.NULL_KEY,  // 等级2语音报警发送指令
    val voiceindex3: String = IOTConstants.NULL_KEY,  // 等级3语音报警发送指令
    val voiceindex4: String = IOTConstants.NULL_KEY,  // 等级4语音报警发送指令
    val voiceindex5: String = IOTConstants.NULL_KEY,  // 消警语音报警发送指令
    val respindex1: String = IOTConstants.NULL_KEY,   // 等级1语音报警应答指令
    val respindex2: String = IOTConstants.NULL_KEY,   // 等级2语音报警应答指令
    val respindex3: String = IOTConstants.NULL_KEY,   // 等级3语音报警应答指令
    val respindex4: String = IOTConstants.NULL_KEY,   // 等级4语音报警应答指令
    val respindex5: String = IOTConstants.NULL_KEY,   // 消警应答指令
    val cleargaptime: String = IOTConstants.NULL_KEY // 消警间隔时间 数值≥1，单位：分钟

) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value.toString().trim()}" }
    }
} 