package com.shmedo.configlibrary.iot.cmd.entity.adme

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/29/20 <br></br>
 * 描述：     生成ADME 执行机构配置参数拼接指令
 */
class AdmeExecutiveAgencyEntity : Validater {
    var meastype //测量方式（0:实时测量，1:整时整点测量，2:定时定点测量）
            : String? = null
    var datatype //数据解算方式（0:顶部固定法，1底部固定法）
            : String? = null
    var datareply //数据应答（0:关闭，1:启用）
            : String? = null
    var roundwaitetime //每轮等待时间
            : String? = null
    var roundmeasinval //每轮测量间隔
            : String? = null
    var invalday //间隔天数
            : String? = null
    var roundmeasstart //每轮测量开始时间
            : String? = null
    var datainval //数据读取间隔
            : String? = null
    var compensatetime //测量补偿时间
            : String? = null
    var driveaddress //电机驱动器地址
            : String? = null
    var downspeed //电机下放速度
            : String? = null
    var interdeep //测斜管孔深
            : String? = null
    var downwaitetime //下放等待时间
            : String? = null
    var upspeed //电机上拉速度
            : String? = null
    var measpacing //测量间距
            : String? = null
    var meaintertime //测量间隔时间
            : String? = null
    var meabaseth //测量基准深度
            : String? = null
    var dwonblocked //下放堵转预判（0:关闭，1:开启）
            : String? = null
    var untimenum //堵转单位时间脉冲数
            : String? = null
    var detectiontime //堵转检测判断时间
            : String? = null
    var detectionstart //堵转检测起点
            : String? = null
    var detectionend //堵转检测终点
            : String? = null
    var interval_compensation //管口安全距离
            : String? = null
    var bottom_safe_distance //管底安全距离
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