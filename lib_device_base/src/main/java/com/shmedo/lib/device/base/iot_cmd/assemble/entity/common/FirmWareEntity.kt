package com.shmedo.lib.device.base.iot_cmd.assemble.entity.common

import com.shmedo.lib.core.util.jsonhelper.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/5/8
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class FirmWareEntity(
    var url: String = IOTConstants.NULL_KEY,//可用下载连接
    var size: String = IOTConstants.NULL_KEY,//固件大小(单位是字节)  266265
    var md5: String = IOTConstants.NULL_KEY,//固件MD5值  83fc8d6764854f9d4f3d02078e9ccb56
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
