package com.shmedo.lib.device.base.iot_cmd.entity.e40

import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/1/21 <br></br>
 * 描述：     生成板卡解算参数拼接指令
 */
class E40BoardSolutionEntity : Validater {
    var inittime //初始化时间
            : String? = null
    var calcgap //解算时间
            : String? = null
    var smoothlevel //平滑等级
            : String? = null
    var reinit //重新初始化
            : String? = null
    var rtkdynamicmode //RTK动态模式
            : String? = null
    var corrval //形变修正值
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (value != null && value != "NullKey") {
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