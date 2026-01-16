package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/13
 * @desc: 功能开关参数查询响应模型
 *
 * 对应指令: md_sensor
 * 查询发送: $cmd=md_sensor&method=0
 * 查询应答: $cmd=md_sensor&method=0&switch=1
 *
 * 参数说明:
 * - method: 方法类型
 *   - 0: 查询参数
 *   - 1: 设置参数
 * - switch: 功能开关
 *   - 0: 关
 *   - 1: 开
 */
@JsonClass(generateAdapter = true)
data class MdSensorData(
    var method: String = "0",   // 方法类型: 0-查询, 1-设置
    var switch: String = "0"    // 功能开关: 0-关, 1-开
)
