package com.shmedo.lib.device.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述：  Das 倾角计信息 X、Y、Z 轴角度、加速度
 */
data class InclinometerInfo(
    val status: String = "",
    val xAxis: String = "",
    val yAxis: String = "",
    val zAxis: String = "",
    val xAcceleration: String = "",
    val yAcceleration: String = "",
    val zAcceleration: String = "",
)
