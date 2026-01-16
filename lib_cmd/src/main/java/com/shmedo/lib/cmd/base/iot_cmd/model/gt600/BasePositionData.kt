package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: GT600 基站位置信息响应数据模型
 *
 * 对应指令: md_getbaseposition
 * 应答示例: $cmd=md_getbaseposition&mode=1&lat=0.000000&lon=0.000000&alt=0.000000
 *
 * 字段说明:
 * - mode: 工作模式，0 表示自动模式，1 表示手动模式
 * - lat: 纬度（度）
 * - lon: 经度（度）
 * - alt: 高程/高度（米）
 */
@JsonClass(generateAdapter = true)
data class BasePositionData(
    /** 工作模式：0-自动模式，1-手动模式 */
    var mode: String = IOTConstants.NULL_KEY,
    
    /** 纬度（度） */
    var lat: String = IOTConstants.NULL_KEY,
    
    /** 经度（度） */
    var lon: String = IOTConstants.NULL_KEY,
    
    /** 高程/高度（米） */
    var alt: String = IOTConstants.NULL_KEY
)
