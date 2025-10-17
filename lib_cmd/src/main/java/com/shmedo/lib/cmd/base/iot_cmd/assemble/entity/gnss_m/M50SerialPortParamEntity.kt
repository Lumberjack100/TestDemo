package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gnss_m

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/9/10
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class M50SerialPortParamEntity(
    val method: String = "1",//0：获取当前参数信息  1：设置参数
    val out_power: String = IOTConstants.NULL_KEY, //外部供电  开关  0 关闭 1 开启
    val rs232_mode: String = IOTConstants.NULL_KEY, //232 外接设备 0：无  1：抓拍相机  2：卫星通信终端
    val cam_module: String = IOTConstants.NULL_KEY, //抓拍频率（分钟）： "15", "30", "60", "120"
    val pixx: String = IOTConstants.NULL_KEY, //抓拍图片水平分辨率
    val pixy: String = IOTConstants.NULL_KEY, //抓拍图片垂直分辨率
    val rs485_mode: String = IOTConstants.NULL_KEY, //485 外接设备  0：无  1：压电式雨量计
    val rs485_baud: String = IOTConstants.NULL_KEY, //485 波特率
    val rs485_addr: String = IOTConstants.NULL_KEY, //485 外接设备地址
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}