package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/27 <br/>
 * 描述：     上报方式
 */
data class MRReportMethod(
    var type: String = "1",//上报方式 1 定时定点上报  2 固定间隔上报
    var interval: String = "",//上报间隔  分钟,数字
    var basis: String = "0",//上报起始时间(基准时间)   数字,   0-23点(小时)  固定间隔上报下需不需要一个起始时间
)
