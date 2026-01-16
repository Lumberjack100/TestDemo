package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: GT600 基站位置设置实体类
 *
 * 对应指令: md_setbaseposition
 * 发送示例: $cmd=md_setbaseposition&mode=1&lat=31.758980&lon=119.645485&alt=47.352
 *
 * 字段说明:
 * - mode: 工作模式，0 表示自动模式，1 表示手动模式
 * - lat: 纬度（度）
 * - lon: 经度（度）
 * - alt: 高程/高度（米）
 */
@JsonClass(generateAdapter = true)
data class BasePositionConfigEntity(
    /** 工作模式：0-自动模式，1-手动模式 */
    val mode: String = IOTConstants.NULL_KEY,
    
    /** 纬度（度） */
    val lat: String = IOTConstants.NULL_KEY,
    
    /** 经度（度） */
    val lon: String = IOTConstants.NULL_KEY,
    
    /** 高程/高度（米） */
    val alt: String = IOTConstants.NULL_KEY
) {
    /**
     * 将实体转换为指令参数字符串
     * 
     * @return 格式如: mode=1&lat=31.758980&lon=119.645485&alt=47.352
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
