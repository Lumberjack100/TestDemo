package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/08
 * 描述：GT600 网络信息 Bean
 *
 * 对应 JSON 中的 net 字段
 */
@JsonClass(generateAdapter = true)
data class GT600NetBean(
    @Json(name = "4g")
    val `_4g`: String = "",        // 4G 是否启用 (on/off)
    val type: String = "",         // 网络类型 (4G/2G)
    val isp: String = "",          // 运营商 (CHN-CT/CHINA MOBILE/CHN-UNICOM)
    val csq: String = "",              // 信号强度 (需要加负号)
    val socket1: String = "",        // 数据中心 1 (0:未启用 1:已连接 2:未连接)
    val socket2: String = "",         // 数据中心 2
    val socket3: String = "",          // 数据中心 3
    val socket4:String = "",          // 数据中心 4
)
