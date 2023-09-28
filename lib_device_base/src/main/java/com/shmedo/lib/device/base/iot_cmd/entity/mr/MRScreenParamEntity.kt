package com.shmedo.lib.device.base.iot_cmd.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/28 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
class MRScreenParamEntity (
    val interval: String = "",//屏幕更新周期  秒 ,数字
    val otime: String = "",//亮屏时间   秒 ,数字
    val ptime: String = "",//通电时间   秒 ,数字
    val bproport: String = "10",//屏幕亮度设置  数字(10-100), 转换比例  10%-100%
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}