package com.shmedo.configlibrary.iot.cmd.entity.hac

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/11 <br></br>
 * 描述：     TODO
 */
class HacMeasuringDataInfoEntity : Validater {
     var equipmodel //电机工作标识 0：停止 1：开始测量 2: 异常
            : String? = null
     var address //MAC 地址
            : String? = null
     var downwaitetime //下放等待时间
            : String? = null
     var datatype //数据解算方式（0:顶部固定法，1底部固定法）
            : String? = null
     var onewaytest //单向测量 0 :关闭 1:开启
            : String? = null
     var holeno //孔号
            : String? = null
     var areano //区号
            : String? = null
     var holedepth //测斜管孔深
            : String? = null
     var checkreverse //反转自检
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