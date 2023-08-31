package com.shmedo.lib.device.base.iot_cmd.entity.adme

import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/2/21 <br></br>
 * 描述：     生成ADME 电机运动堵转缓停参数拼接指令
 */
class AdmeLockedRotorDetectionEntity : Validater {
    var lowtbtss //下放堵转缓停（0:关闭，1:开启）
            : String? = null
    var numpput //单位时间脉冲数
            : String? = null
    var pdajtime //脉冲检测判断时间
            : String? = null
    var detintiona //堵转检测区间起始值(匀速阶段)
            : String? = null
    var detintionb //堵转检测区间终值(匀速阶段)
            : String? = null
    var lowtorblothr //下放力矩堵转阈值
            : String? = null
    var lowtordetime //下放力矩检测判断时间
            : String? = null
    var lowsusrana //下放缓停区间起始值(减速阶段)
            : String? = null
    var lowsusranb //下放缓起区间终值(加速阶段)
            : String? = null
    var uptbtss //上拉堵转缓停（0:关闭，1:开启）
            : String? = null
    var uptorblothr //上拉力矩堵转阈值
            : String? = null
    var uptordetime //上拉力矩检测判断时间
            : String? = null
    var upsusrana //上拉缓停区间起始值(减速阶段)
            : String? = null
    var upsusranb //上拉缓起区间终值(加速阶段)
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