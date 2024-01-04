package com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/1/4
 * 描述： 生成ADME导槽校准配置参数拼接指令
 */
@JsonClass(generateAdapter = true)
data class AdmeGuideGrooveCalibrationEntity(
    var movementway: String = "", //运动方式（0:正转，1:反转）
    var motorspeed: String = IOTConstants.NULL_KEY, //电机速度
    var movepulse: String = IOTConstants.NULL_KEY, //运动脉冲数
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
