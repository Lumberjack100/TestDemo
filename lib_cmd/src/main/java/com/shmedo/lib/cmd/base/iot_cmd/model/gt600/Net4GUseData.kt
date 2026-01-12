package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: 4G 网络使用开关查询响应模型
 *
 * 对应指令: md_getnet4guse
 * 应答示例: $cmd=md_getnet4guse&use=1
 *
 * 参数说明:
 * - use: 4G 功能开关状态
 *   - 0: 关闭4G功能
 *   - 1: 开启4G功能
 */
@JsonClass(generateAdapter = true)
data class Net4GUseData(
    var use: String = "0"  // 4G开关状态: 0-关闭, 1-开启
)
