package com.shmedo.lib.device.base.iot_cmd.assemble.entity.common

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/4/25
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class RtkParamEntity(
    val method: String = "1",//0：获取当前参数信息  1：设置参数
    val mode: String = "", //当前gnss模式 1:固定站  2:移动站  默认2
    val sw: String = IOTConstants.NULL_KEY, //解算盒子使用配置开关 0:停止使用解算盒子  1:开始使用解算盒子  默认0
    val frontCalc: String = IOTConstants.NULL_KEY, //前端解算开关 0:关闭前端解算  1:打开前端解算  默认0
    val baseStationMode: String = IOTConstants.NULL_KEY, //基站坐标模式 0:以精确坐标设置基站模式  1:以自主优化方式设置基准站模式  默认0
    val latitude: String = IOTConstants.NULL_KEY, //纬度 基站纬度  （-90~90） 单位 度
    val longitude: String = IOTConstants.NULL_KEY, //经度 基站经度  （-180~180）单位 度
    val height: String = IOTConstants.NULL_KEY, //海拔高度 基站高度  （-30000~30000）单位米
    val distance: String = IOTConstants.NULL_KEY, //距离 自主优化距离 （0~10） 单位 米
    val time: String = IOTConstants.NULL_KEY, //gnss前端解算处理时间 自主优化时间 单位 秒
    val id: String = IOTConstants.NULL_KEY, //基准站ID号 用于作移动站、基站细分
    val gateAngleVal1: String = IOTConstants.NULL_KEY, //倾角报警一级阈值 单位 度
    val gateAngleVal2: String = IOTConstants.NULL_KEY, //倾角报警二级阈值
    val gateAngleVal3: String = IOTConstants.NULL_KEY, //倾角报警三级阈值
    val gateAngleVal4: String = IOTConstants.NULL_KEY, //倾角报警四级阈值
    val gateDevVal1: String = IOTConstants.NULL_KEY, //位移报警一级阈值  单位 mm
    val gateDevVal2: String = IOTConstants.NULL_KEY, //位移报警二级阈值
    val gateDevVal3: String = IOTConstants.NULL_KEY, //位移报警三级阈值
    val gateDevVal4: String = IOTConstants.NULL_KEY, //位移报警四级阈值
    val rtkMode: String = IOTConstants.NULL_KEY, //解算模式源 1:静态解算  2:动态结算  默认1
    val obs: String = IOTConstants.NULL_KEY, //观测数据上报频率 取值为[0-60]
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}

