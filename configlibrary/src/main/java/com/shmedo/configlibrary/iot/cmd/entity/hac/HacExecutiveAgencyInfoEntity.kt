package com.shmedo.configlibrary.iot.cmd.entity.hac

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/29 <br></br>
 * 描述：     TODO
 */
class HacExecutiveAgencyInfoEntity : Validater {
     var datatype //数据解算方式（0:顶部固定法，1底部固定法）
            : String? = null
     var datareply //数据应答（0:关闭，1:启用）
            : String? = null
     var datainval //数据读取间隔
            : String? = null
     var compensatetime //测量补偿时间
            : String? = null
     var driveaddress //电机驱动器地址
            : String? = null
     var downspeed //电机下放速度
            : String? = null
     var downwaitetime //下放等待时间
            : String? = null
     var upspeed //电机上拉速度
            : String? = null
     var measpacing //测量间距
            : String? = null
     var meaintertime //测量间隔时间
            : String? = null
     var interval_compensation //距离补偿区间h1
            : String? = null
     var interval_fitting //数据拟合区间h2
            : String? = null
     var point_offset //测点偏移距离h3
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