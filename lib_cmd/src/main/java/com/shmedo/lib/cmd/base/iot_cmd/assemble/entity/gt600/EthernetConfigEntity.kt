package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: 以太网配置设置实体类
 *
 * 对应指令: md_seteth0
 * 发送示例: $cmd=md_seteth0&dhcp=0&ip=172.168.5.241&gateway=172.168.5.254&dns=8.8.8.8
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
data class EthernetConfigEntity(
    val dhcp: String = IOTConstants.NULL_KEY,     // IP分配方式: 0-手动, 1-自动
    val ip: String = IOTConstants.NULL_KEY,       // IP地址
    val gateway: String = IOTConstants.NULL_KEY,  // 网关
    val dns: String = IOTConstants.NULL_KEY       // 首选DNS
) {
    /**
     * 将实体转换为指令参数字符串
     * @return 格式如: dhcp=0&ip=172.168.5.241&gateway=172.168.5.254&dns=8.8.8.8
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
