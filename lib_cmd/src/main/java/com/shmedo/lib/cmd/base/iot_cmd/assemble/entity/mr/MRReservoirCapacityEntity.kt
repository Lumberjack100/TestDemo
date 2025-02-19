package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2025/1/23
 * 描述： 库容计算参数
 */
@JsonClass(generateAdapter = true)
data class MRReservoirCapacityEntity(
    val switch: String = "1",//是否开启 0:关闭 1:开启
    val count: String = IOTConstants.NULL_KEY,//坐标点数量 最多配置50个，最低配置3个，1个坐标点含1个X轴坐标值和1个Y轴坐标值
    val xparam: String = IOTConstants.NULL_KEY,//X轴坐标值 1位小数，单位m
    val yparam: String = IOTConstants.NULL_KEY//Y轴坐标值 1位小数，单位m
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
