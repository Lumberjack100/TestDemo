package com.shmedo.lib.device.base.iot_cmd.entity.das

import com.shmedo.lib.device.base.Validater


/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：      生成报警级别参数拼接指令
 */
class AlarmLevelEntity : Validater {
    var type //传感器类型  1:雨量计,2:倾角计,3:主传感器
            : String? = null
    var level1 //无报警
            : String? = null
    var level2 //蓝色一级
            : String? = null
    var level3 //黄色二级
            : String? = null
    var level4 //橙色三级
            : String? = null
    var level5 //红色四级
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            stringBuilder.append("type")
            stringBuilder.append("=")
            stringBuilder.append(type)
            stringBuilder.append("&")
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (value != null && f.name != "type" && value != "NullKey") {
                    stringBuilder.append(f.name)
                    stringBuilder.append("=")
                    stringBuilder.append(value)
                    stringBuilder.append("&")
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
        }
        return stringBuilder.toString()
    }
}