package com.shmedo.lib.device.base.iot_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： 报警上报间隔
 */
data class AlarmReportIntervalInfo(
    var level1: String = "",//一级报警上报间隔  默认60,单位s
    var level2: String = "",//二级报警上报间隔 默认300,单位s
    var level3: String = "",//三级报警上报间隔 默认1800,单位s
    var level4: String = "",//四级报警上报间隔 默认3600,单位s
    var location: String = "",//位置信息上报间隔  [不限]默认7200,单位s
    var heartbeat: String = "",//心跳包上报间隔  [1-36000]默认60，单位s
    var collect: String = "",//采集间隔  [1-7200]默认30，单位s
    var reptgap: String = "",//正常上报周期  [1-36000]默认120，单位min
)
