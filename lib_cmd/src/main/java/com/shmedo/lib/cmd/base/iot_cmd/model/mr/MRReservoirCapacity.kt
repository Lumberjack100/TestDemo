package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者：gonghe
 * 创建时间：2025/1/23
 * 描述： TODO
 */
data class MRReservoirCapacity(
    var switch: String = "",//是否开启 0:关闭 1:开启
    var count: String = "",//坐标点数量 最多配置50个，最低配置3个，1个坐标点含1个X轴坐标值和1个Y轴坐标值
    var xparam: String = "",//X轴坐标值 1位小数，单位m
    var yparam: String = ""//Y轴坐标值 1位小数，单位m
)
