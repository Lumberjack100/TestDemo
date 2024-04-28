package com.shmedo.lib.device.base.iot_cmd.model.das

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/20 <br></br>
 * 描述：     数据上报时间
 */
data class DasDataReportInfo(
    var report_intv: String = "", //数据上报间隔
    var plus_intv: String = "", //加报间隔
    var plus_count: String = "", //加报次数
)