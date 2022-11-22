package com.shmedo.configlibrary.iot.cmd.entity.hac

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/20 <br></br>
 * 描述：     预警值
 */
class HacWarningValueEntity : Validater {
     var x1min //一级预警 X 轴最小值
            : String? = null
     var x1max //一级预警 X 轴最大值
            : String? = null
     var y1min //一级预警 Y 轴最小值
            : String? = null
     var y1max //一级预警 Y 轴最大值
            : String? = null
     var x2min //二级预警 X 轴最小值
            : String? = null
     var x2max //二级预警 X 轴最大值
            : String? = null
     var y2min //二级预警 Y 轴最小值
            : String? = null
     var y2max //二级预警 Y 轴最大值
            : String? = null
     var x3min //三级预警 X 轴最小值
            : String? = null
     var x3max //三级预警 X 轴最大值
            : String? = null
     var y3min //三级预警 Y 轴最小值
            : String? = null
     var y3max //三级预警 Y 轴最大值
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