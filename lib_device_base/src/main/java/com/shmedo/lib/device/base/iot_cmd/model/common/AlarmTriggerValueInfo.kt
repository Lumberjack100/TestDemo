package com.shmedo.lib.device.base.iot_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： 报警阈值
 */
data class AlarmTriggerValueInfo(
    var level1: String = "",//一级报警倾角阈值  默认40
    var level2: String = "",//二级报警倾角阈值  默认20
    var level3: String = "",//三级报警倾角阈值  默认10
    var level4: String = "",//四级报警倾角阈值  默认5
    var devlevel1: String = "",//一级报警位移阈值  默认40
    var devlevel2: String = "",//二级报警位移阈值  默认20
    var devlevel3: String = "",//三级报警位移阈值  默认10
    var devlevel4: String = "",//四级报警位移阈值  默认5
)
