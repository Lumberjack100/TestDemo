package com.shmedo.lib.device.base.iot_cmd.assemble.entity.hac

import com.shmedo.lib.core.util.jsonhelper.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class HacMeasuringDataEntity(
    val equipmodel: String = "", //电机工作标识  0: 重新开始测量 1：继续测量
    val address: String = IOTConstants.NULL_KEY, //MAC 地址
    val downwaitetime: String = IOTConstants.NULL_KEY, // 下放等待时间
    var datatype: String = IOTConstants.NULL_KEY, //数据解算方式（0:顶部固定法，1底部固定法）
    var onewaytest: String = IOTConstants.NULL_KEY, //单向测量 0 :关闭 1:开启
    val holeno: String =IOTConstants.NULL_KEY, //孔号
    val areano: String = IOTConstants.NULL_KEY,//区号
    val holedepth: String = IOTConstants.NULL_KEY,//测斜管孔深
    val checkreverse: String = IOTConstants.NULL_KEY,//反转自检
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
