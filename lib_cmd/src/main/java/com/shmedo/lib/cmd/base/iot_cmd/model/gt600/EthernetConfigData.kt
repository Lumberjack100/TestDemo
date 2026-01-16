package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: 以太网配置查询响应模型
 *
 * 对应指令: md_geteth0
 * 应答示例: $cmd=md_geteth0&dhcp=0&ip=172.168.5.241&gateway=172.168.5.254&dns=8.8.8.8
 *
 * 参数说明:
 * - dhcp: IP分配方式
 *   - 0: 手动分配（当前仅支持手动）
 *   - 1: 自动分配（不支持）
 * - ip: IP地址
 * - gateway: 网关
 * - dns: 首选DNS
 */
@JsonClass(generateAdapter = true)
data class EthernetConfigData(
    var dhcp: String = "0",      // IP分配方式: 0-手动, 1-自动
    var ip: String = "",         // IP地址
    var gateway: String = "",    // 网关
    var dns: String = ""         // 首选DNS
)
