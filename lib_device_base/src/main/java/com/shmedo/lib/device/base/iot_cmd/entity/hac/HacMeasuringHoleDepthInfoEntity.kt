package com.shmedo.lib.device.base.iot_cmd.entity.hac

import com.shmedo.lib.device.base.Validater


/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/22 <br></br>
 * 描述：     TODO
 */
class HacMeasuringHoleDepthInfoEntity : Validater {
     var model //电机工作标识  0: 重新开始测量 1：继续测量
            : String? = null
     var address //MAC 地址
            : String? = null
     var holeno //孔号
            : String? = null
     var areano //区号
            : String? = null
     var lowtbtss //下放堵转检测（0:关闭，1:开启）
            : String? = null
     var motorspeed //电机速度
            : String? = null
     var measway //测量模式（0:自动测量，1:手动测量）
            : String? = null
     var safedistance //管底补偿距离
            : String? = null
     var movementway //运动方式（0:上拉，1:下放）
            : String? = null
     var movedistance //设定运动距离
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