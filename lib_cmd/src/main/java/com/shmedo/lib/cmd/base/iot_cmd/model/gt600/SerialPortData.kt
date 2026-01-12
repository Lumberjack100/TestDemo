package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: 串口参数查询响应模型
 *
 * 对应指令: md_getdbguart
 * 应答示例: $cmd=md_getdbguart&type=1&baud=9600
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
data class SerialPortData(
    var type: String = "1",    // 功能码: 1-传感器采集, 2-NMEA输出, 3-差分数据输入, 4-差分数据输出, 5-原始数据输出, 6-解算结果输出
    var baud: String = "9600"  // 波特率: 9600, 19200, 38400, 57600, 115200
)
