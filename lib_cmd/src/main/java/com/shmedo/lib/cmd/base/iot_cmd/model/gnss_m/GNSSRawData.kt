package com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * @author：gonghe
 * @time: 2025/1/22
 * @desc: GNSS 原始数据模型
 * 响应示例：$cmd=md_getgnssraw&result=succ&ephes=60&obs=5&msmp=0&msmlevel=255&obslevel=0&datatype=1&mmpltype=0&rtkthdtype=4&adrthdtype=4&apikey=...&msgid=...
 */
data class GNSSRawData(
    val ephes: String = IOTConstants.NULL_KEY, // 星历数据
    val obs: String = IOTConstants.NULL_KEY, // 采样率
    val msmp: String = IOTConstants.NULL_KEY, // 其他参数
    val msmlevel: String = IOTConstants.NULL_KEY,
    val obslevel: String = IOTConstants.NULL_KEY,
    val datatype: String = IOTConstants.NULL_KEY,
    val mmpltype: String = IOTConstants.NULL_KEY,
    val rtkthdtype: String = IOTConstants.NULL_KEY,
    val adrthdtype: String = IOTConstants.NULL_KEY
) 