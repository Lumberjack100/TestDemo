package com.shmedo.lib.device.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述：  Das 倾角计信息 X、Y、Z 轴角度、加速度
 */
data class InclinometerInfo(
    val status: String = "", // 状态 0正常，1异常，2 不展示
    val xAxis: String = "",
    val yAxis: String = "",
    val zAxis: String = "",
    val xAcceleration: String = "",
    val yAcceleration: String = "",
    val zAcceleration: String = "",
)
