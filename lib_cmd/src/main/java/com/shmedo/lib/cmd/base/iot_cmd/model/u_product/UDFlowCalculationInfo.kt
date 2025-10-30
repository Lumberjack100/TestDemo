package com.shmedo.lib.cmd.base.iot_cmd.model.u_product

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述： 一体式泥位计流量计算配置参数
 */
data class UDFlowCalculationInfo(
    var shape: String = IOTConstants.NULL_KEY, // 截面形状
    var cannalwide: String = IOTConstants.NULL_KEY, // 截面宽度 / 上宽
    var initdepth: String = IOTConstants.NULL_KEY, // 初始水深
    var initheight: String = IOTConstants.NULL_KEY, // 初始空高
    var maxdepth: String = IOTConstants.NULL_KEY, // 截面深度 / 高度
    var bottomwide: String = IOTConstants.NULL_KEY, // 截面下宽
    var sloperatio: String = IOTConstants.NULL_KEY, // 边坡系数
    var diameter: String = IOTConstants.NULL_KEY, // 截面直径 / U 型直径
    var customize: String = IOTConstants.NULL_KEY, // 自定义参数
    var hcorvalue: String = IOTConstants.NULL_KEY, // 空高修正
)
