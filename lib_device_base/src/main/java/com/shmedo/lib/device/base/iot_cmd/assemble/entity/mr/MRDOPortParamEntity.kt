package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRDOPortParamEntity(
    var kstatus1: String = IOTConstants.NULL_KEY,//1 开 0关
    var kstatus2: String = IOTConstants.NULL_KEY,//1 开 0关
    var kstatus3: String = IOTConstants.NULL_KEY,//1 开 0关
    var kstatus4: String = IOTConstants.NULL_KEY,//1 开 0关
    var kstatus5: String = IOTConstants.NULL_KEY,//1 开 0关
    var kstatus6: String = IOTConstants.NULL_KEY,//1 开 0关
    var kstatus7: String = IOTConstants.NULL_KEY,//1 开 0关
    var kstatus8: String = IOTConstants.NULL_KEY,//1 开 0关
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}