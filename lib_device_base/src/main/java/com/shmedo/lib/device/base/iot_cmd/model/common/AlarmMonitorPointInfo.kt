package com.shmedo.lib.device.base.iot_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： 报警监控点信息
 */
data class AlarmMonitorPointInfo(
    var sw: String = "", //报警开关  0 关闭 1 开启 默认 0
    var alarm_send_min_gap: String = "",//最短发送间隔
    var alarm_resend_cnt: String = "",//发送重复次数  默认3
    var alarm_resend_gap: String = "",//重发间隔  默认5秒
    var monitorpoint: String = "",//监测点编号 [1~15] 默认01
    var cnt: String = "",//报警播报次数  [0~255] 其中0表示关闭当前报警，255表示一直报警，默认03
    var level1: String = "",//一级报警语音编号  [1~255] 默认 4
    var level2: String = "",//二级报警语音编号  [1~255] 默认 3
    var level3: String = "",//三级报警语音编号  [1~255] 默认 2
    var level4: String = "",//四级报警语音编号  [1~255] 默认 1
)
