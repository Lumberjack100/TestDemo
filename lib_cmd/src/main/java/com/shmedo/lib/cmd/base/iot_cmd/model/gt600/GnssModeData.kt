package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/14
 * @desc: GNSS工作模式（站点类型）查询响应模型
 *
 * 对应指令: md_gnssmode
 * 查询发送: $cmd=md_gnssmode&method=0
 * 查询应答: $cmd=md_gnssmode&method=0&station=1
 *
 * 参数说明:
 * - method: 方法类型
 *   - 0: 查询参数
 *   - 1: 设置参数
 * - station: 站点类型
 *   - 0: 基站
 *   - 1: 测站
 */
@JsonClass(generateAdapter = true)
data class GnssModeData(
    var method: String = "0",    // 方法类型: 0-查询, 1-设置
    var station: String = "0"    // 站点类型: 0-基站, 1-测站
)
