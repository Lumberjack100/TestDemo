package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: 双天线参数设置实体类
 *
 * 对应指令: md_cfgnmeavtgout&method=1
 * 发送示例: $cmd=md_cfgnmeavtgout&method=1&switch=0&antdist=50.44&report_freq=1
 *
 * 参数说明:
 * - method: 功能选择 (0-获取参数, 1-配置参数)
 * - switch: 功能开关 (0-关闭, 1-开启)
 * - antdist: 天线距离，单位厘米，数值大于零
 * - report_freq: 输出频率，单位秒，支持 1,5,10,15,30,60 选择
 */
@JsonClass(generateAdapter = true)
data class DualAntennaConfigEntity(
    val method: String = "1",                       // 功能选择: 1-配置参数
    val switch: String = IOTConstants.NULL_KEY,     // 功能开关
    val antdist: String = IOTConstants.NULL_KEY,    // 天线距离(厘米)
    val report_freq: String = IOTConstants.NULL_KEY // 上报频率(秒)
) {
    /**
     * 将实体转换为指令参数字符串
     * @return 格式如: method=1&switch=0&antdist=50.44&report_freq=1
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
