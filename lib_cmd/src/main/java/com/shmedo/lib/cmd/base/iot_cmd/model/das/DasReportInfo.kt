package com.shmedo.lib.cmd.base.iot_cmd.model.das

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/11/18 <br/>
 * 描述：      定时定点上报参数
 */
data class DasReportInfo(
    var type: String= "0", //上报方式  0 固定间隔上报 1 定时定点上报
    var timepoint: String= "0",//上报起始时间(基准时间)   0-23点(小时)  固定间隔上报下需不需要一个起始时间
    var timegap: String= "",//上报间隔  分钟
)