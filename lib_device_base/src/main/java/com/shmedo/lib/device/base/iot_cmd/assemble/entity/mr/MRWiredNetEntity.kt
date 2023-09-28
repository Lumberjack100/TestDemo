package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
class MRWiredNetEntity(
    val switch: String = "1",//是否开启以太网 0:关闭 1:开启
    val dhcp: String = IOTConstants.NULL_KEY,//是否开启DHCP 0:关闭 1:开启
    val ipaddr: String = IOTConstants.NULL_KEY,//IP地址
    val mask: String = IOTConstants.NULL_KEY,//子网掩码
    val gateway: String = IOTConstants.NULL_KEY,//网关
    val dns: String = IOTConstants.NULL_KEY,//首选DNS
    val dnss: String = IOTConstants.NULL_KEY//备用DNS
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}