package com.shmedo.lib.device.base.iot_cmd.entity

import com.shmedo.lib.core.util.MoshiUtil
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
open class BaseEntity {
    open fun toCommandString(): String {
        val jsonMap: Map<String, Any> = MoshiUtil.toJson(this) as Map<String, Any>

        return jsonMap.entries
            .filterNot { it.value == "NullKey" }// 过滤值为 "NullKey" 的条目
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}