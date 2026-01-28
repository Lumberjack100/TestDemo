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
data class LoraCommunicateEntity(
    val loratype: String = IOTConstants.NULL_KEY,//LORA模组型号 1：F8L10C  2：TP1107
    val airbaud: String = IOTConstants.NULL_KEY, //空中速率  [1~6] 默认3
    val chl: String = IOTConstants.NULL_KEY,//信道 [0~19] 载波频率以410Mhz为起始，间隔1Mhz，进行信道划分，共划分30个信道，默认10
    val outpwr: String = IOTConstants.NULL_KEY,//发射功率 [5~20] 默认20
    val netid: String = IOTConstants.NULL_KEY,//网络号 [1~10] 默认1
    val localid: String = IOTConstants.NULL_KEY,//本机地址 [1~20] 网关默认1,监测设备默认2
    val dstid: String = IOTConstants.NULL_KEY,//目标地址 [1~20] 网关默认2,监测设备默认1
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
