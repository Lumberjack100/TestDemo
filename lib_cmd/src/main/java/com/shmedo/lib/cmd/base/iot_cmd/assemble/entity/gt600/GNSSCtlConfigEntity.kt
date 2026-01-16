package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: GNSS 控制参数（RTCM参数）设置实体类
 *
 * 对应指令: md_setgnssctl
 * 发送示例: $cmd=md_setgnssctl&encrypttype=0&rtcmobstime=15&rtcmephtime=60&onlybd=0
 *
 * 参数说明:
 * - encrypttype: 是否启用加密 (0-不加密, 1-软加密, 2-硬加密)
 * - rtcmobstime: RTCM 观测数据输出频率，单位秒，取值 1,5,10,15,30
 * - rtcmephtime: RTCM 星历数据输出频率，单位秒，取值 1,5,10,15,30,60
 * - onlybd: 单北斗功能 (0-不开启, 1-开启)
 */
@JsonClass(generateAdapter = true)
data class GNSSCtlConfigEntity(
    val encrypttype: String = IOTConstants.NULL_KEY, // 加密类型
    val rtcmobstime: String = IOTConstants.NULL_KEY, // RTCM 观测数据输出频率(秒)
    val rtcmephtime: String = IOTConstants.NULL_KEY, // RTCM 星历数据输出频率(秒)
    val onlybd: String = IOTConstants.NULL_KEY       // 单北斗功能
) {
    /**
     * 将实体转换为指令参数字符串
     * @return 格式如: encrypttype=0&rtcmobstime=15&rtcmephtime=60&onlybd=0
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
