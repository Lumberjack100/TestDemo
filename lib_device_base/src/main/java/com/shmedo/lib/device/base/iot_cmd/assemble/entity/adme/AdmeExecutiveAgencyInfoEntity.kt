package com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/19
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class AdmeExecutiveAgencyInfoEntity(
    val meastype: String = "", //测量方式（0:实时测量，1:整时整点测量，2:定时定点测量）
    val datatype: String = "", //数据解算方式（0:顶部固定法，1底部固定法）
    val datareply: String = "",//数据应答（0:关闭，1:启用）
    val roundwaitetime: String = IOTConstants.NULL_KEY, //每轮等待时间
    val roundmeasinval: String = IOTConstants.NULL_KEY, //每轮测量间隔
    val invalday: String = IOTConstants.NULL_KEY, //间隔天数
    val roundmeasstart: String = IOTConstants.NULL_KEY, //每轮测量开始时间
    val datainval: String = IOTConstants.NULL_KEY, //数据读取间隔
    val compensatetime: String = IOTConstants.NULL_KEY, //测量补偿时间
    val driveaddress: String = IOTConstants.NULL_KEY,//电机驱动器地址
    val downspeed: String = IOTConstants.NULL_KEY,//电机下放速度
    val interdeep: String = IOTConstants.NULL_KEY, //测斜管孔深
    val downwaitetime: String = IOTConstants.NULL_KEY,//下放等待时间
    val upspeed: String = IOTConstants.NULL_KEY,//电机上拉速度
    val measpacing: String =IOTConstants.NULL_KEY, //测量间距
    val meaintertime: String = IOTConstants.NULL_KEY, //测量间隔时间
    val meabaseth: String = IOTConstants.NULL_KEY, //测量基准深度
    val dwonblocked: String = IOTConstants.NULL_KEY,//下放堵转预判（0:关闭，1:开启）
    val untimenum: String = IOTConstants.NULL_KEY,//堵转单位时间脉冲数
    val detectiontime: String = IOTConstants.NULL_KEY,//堵转检测判断时间
    val detectionstart: String = IOTConstants.NULL_KEY,//堵转检测起点
    val detectionend: String = IOTConstants.NULL_KEY, //堵转检测终点
    val interval_compensation: String = IOTConstants.NULL_KEY,//管口安全距离 h1
    val bottom_safe_distance: String = IOTConstants.NULL_KEY, //管底安全距离
    val interval_fitting: String = IOTConstants.NULL_KEY,//数据拟合区间h2
    val point_offset: String = IOTConstants.NULL_KEY,//测点下移距离 h3
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}