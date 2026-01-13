package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/13
 * @desc: 截至高度角参数查询响应模型
 *
 * 对应指令: md_elevation_mask
 * 查询发送: $cmd=md_elevation_mask&method=0
 * 查询应答: $cmd=md_elevation_mask&method=0&angle=15
 *
 * 参数说明:
 * - method: 方法类型
 *   - 0: 查询参数
 *   - 1: 设置参数
 * - angle: 截至高度角
 *   - 取值范围: 5-90°，间隔5°
 *   - 默认值: 15°
 */
@JsonClass(generateAdapter = true)
data class ElevationMaskData(
    var method: String = "0",   // 方法类型: 0-查询, 1-设置
    var angle: String = "15"    // 截至高度角: 5-90°，默认15°
)
