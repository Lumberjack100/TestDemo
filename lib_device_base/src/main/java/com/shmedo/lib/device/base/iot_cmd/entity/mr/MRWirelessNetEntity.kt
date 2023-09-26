package com.shmedo.lib.device.base.iot_cmd.entity.mr

import com.shmedo.lib.device.base.iot_cmd.entity.BaseEntity
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
class MRWirelessNetEntity(
    var switch: String = "1",//是否开启4G 0:关闭 1:开启
    var apn: String = "",
    var username: String = "",
    var password: String = ""
) : BaseEntity(){
    override fun toCommandString(): String {
        return super.toCommandString()
    }
}