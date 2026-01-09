package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: 串口参数设置实体类
 *
 * 对应指令: md_setdbguart
 * 发送示例: $cmd=md_setdbguart&type=1&baud=115200
 *
 * 参数说明:
 * - type: 功能码
 *   - 1: 传感器采集
 *   - 2: NMEA 输出
 *   - 3: 差分数据输入
 *   - 4: 差分数据输出
 *   - 5: 原始数据输出
 *   - 6: 解算结果输出
 * - baud: 波特率 (9600, 19200, 38400, 57600, 115200)
 */
@JsonClass(generateAdapter = true)
data class SerialPortConfigEntity(
    val type: String = IOTConstants.NULL_KEY,  // 功能码
    val baud: String = IOTConstants.NULL_KEY   // 波特率
) {
    /**
     * 将实体转换为指令参数字符串
     * @return 格式如: type=1&baud=9600
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
