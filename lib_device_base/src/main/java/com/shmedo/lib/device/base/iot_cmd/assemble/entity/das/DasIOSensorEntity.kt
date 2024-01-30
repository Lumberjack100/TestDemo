package com.shmedo.lib.device.base.iot_cmd.assemble.entity.das

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/1/30
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class DasIOSensorEntity(
    var type: String = "", //0：关闭开关量功能 1：雨量站模式 2：断线报警器模式
    var value: String = "", //当type取1时，value代表雨量计精度  当type取2时，value代表断线报警器状态，0：常开，1：常关
    var min_time: String = IOTConstants.NULL_KEY,//雨量计翻斗翻转最小间隔
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
