package com.shmedo.lib.cmd.base.iot_cmd.model.mr

import com.squareup.moshi.JsonClass

/**
 * 报警模块参数模型类
 */
@JsonClass(generateAdapter = true)
data class MRAlarmModuleParam(
    var index: String = "",                   // 类型索引号 0：雨量 1：水位
    var switch: String = "",                  // 功能开关 开：1 关：0
    var relay: String = "",                   // 继电器K接口控制 开：1 关：0
    var holdtime: String = "",                // 报警持续时间 数值≥1，单位：分钟
    var gaptime: String = "",                 // 报警间隔时间 数值≥1，单位：分钟
    var cleargaptime: String = "",            // 消警间隔时间 数值≥1，单位：分钟
    var warnlevel: String = "",               // 报警阈值 雨量单位：mm，水位单位：m
    var voiceindex1: String = "",             // 等级1语音报警发送指令
    var voiceindex2: String = "",             // 等级2语音报警发送指令
    var voiceindex3: String = "",             // 等级3语音报警发送指令
    var voiceindex4: String = "",             // 等级4语音报警发送指令
    var voiceindex5: String = "",             // 消警语音报警发送指令
    var respindex1: String = "",              // 等级1语音报警应答指令
    var respindex2: String = "",              // 等级2语音报警应答指令
    var respindex3: String = "",              // 等级3语音报警应答指令
    var respindex4: String = "",              // 等级4语音报警应答指令
    var respindex5: String = ""               // 消警应答指令
) 