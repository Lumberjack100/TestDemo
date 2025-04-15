package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class RadioCommunicateEntity(
    var sw: String = IOTConstants.NULL_KEY, //开关  0 关闭 1 开启
    val airbaud: String = IOTConstants.NULL_KEY, //空中速率  0,1,2 默认 1
    val rxchl: String = IOTConstants.NULL_KEY,//广播接收频点 [0~19] 载波频率以450.15Mhz为起始，间隔1Mhz，进行信道划分，共划分20个信道 自组网网关：接收默认6，发送默认13 M20S：接收默认13，发送默认6
    val txchl: String = IOTConstants.NULL_KEY,//报警发送频点 [0~19] 载波频率以450.15Mhz为起始，间隔1Mhz，进行信道划分，共划分20个信道 自组网网关：接收默认6，发送默认13 M20S：接收默认13，发送默认6
    val outpwr: String = IOTConstants.NULL_KEY,//发射功率 [0~22] 默认22
    val bcchl: String = IOTConstants.NULL_KEY,//RTCM数据频点 [1~19] 默认1 载波频率以450.15Mhz为起始，间隔1Mhz，进行信道划分，共划分20个信道（仅M20S有效，默认为0）
    val localaddr: String = IOTConstants.NULL_KEY,//本机地址 [1100~1140] 默认 1100
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
