
package com.shmedo.lib.device.base.iot_cmd.entity.e40

import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2/25/21 <br></br>
 * 描述：      生成GNSS RTK参数拼接指令
 */
class E40RTKModeEntity : Validater {
    var mode //0表示基站，1表示移动站
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("mode=$mode")
        return stringBuilder.toString()
    }
}