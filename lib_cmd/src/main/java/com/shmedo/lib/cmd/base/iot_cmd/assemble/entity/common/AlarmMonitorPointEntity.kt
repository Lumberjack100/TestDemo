package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class AlarmMonitorPointEntity(
    val sw: String = IOTConstants.NULL_KEY, //报警开关  0 关闭 1 开启 默认 0   仅仅 m20S 设备支持这个字段
    val alarm_send_min_gap: String = IOTConstants.NULL_KEY,//最短发送间隔
    val alarm_resend_cnt: String = IOTConstants.NULL_KEY,//发送重复次数  默认3
    val alarm_resend_gap: String = IOTConstants.NULL_KEY,//重发间隔  默认5秒
    val monitorpoint: String = IOTConstants.NULL_KEY,//监测点编号 [1~15] 默认01
    val cnt: String = IOTConstants.NULL_KEY,//报警播报次数  [0~255] 其中0表示关闭当前报警，255表示一直报警，默认03
    val level1: String = IOTConstants.NULL_KEY,//一级报警语音编号  [1~255] 默认 4
    val level2: String = IOTConstants.NULL_KEY,//二级报警语音编号  [1~255] 默认 3
    val level3: String = IOTConstants.NULL_KEY,//三级报警语音编号  [1~255] 默认 2
    val level4: String = IOTConstants.NULL_KEY,//四级报警语音编号  [1~255] 默认 1
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
