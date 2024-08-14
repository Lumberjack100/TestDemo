package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/2/18
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class AudibleAlarmEntity(
    var alarmstatus: String = "0",//声光报警器开启状态 0 关闭 1 开启
    var screenstatus: String = "0", //电子点阵屏开启状态 0 关闭 1 开启
    var alarmtype: String = IOTConstants.NULL_KEY,//类型  0  降雨量  1 水位
    var alarmaddr: String = IOTConstants.NULL_KEY, //声光报警器地址
    var level1: String = IOTConstants.NULL_KEY, //1级预警值
    var level2: String = IOTConstants.NULL_KEY, //2级预警值
    var level3: String = IOTConstants.NULL_KEY, //3级预警值
    var playtime: String = IOTConstants.NULL_KEY, //播放时长 秒
    var playgap: String = IOTConstants.NULL_KEY, //播放间隙 秒
    var volume: String = IOTConstants.NULL_KEY, //音量大小
    var screenaddr: String = IOTConstants.NULL_KEY, //电子屏地址
    var showtime: String = IOTConstants.NULL_KEY, //电子屏显示时长 秒
    var showgap: String = IOTConstants.NULL_KEY, //电子屏熄屏时长 秒
    var mcuaddr: String = IOTConstants.NULL_KEY, //MCU 地址
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}