package com.shmedo.lib.cmd.base.iot_cmd.model.common

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/27 <br/>
 * 描述：     一体式自供电 GNSS 接收机(M50)上报方式
 */
data class DataReportType(
    var type: String = "",//上报方式 0 固定间隔上报  1 定时定点上报
    var timepoint: String= "0",//上报起始时间(基准时间)   0-23点（小时）  固定间隔上报下需不需要一个起始时间
    var timehour: String = "",//上报起始时间（小时）  数字,   0-23点（小时）  固定间隔上报下需不需要一个起始时间
    var timemin: String = "",//上报起始时间（分钟）  数字,   5-60  固定间隔上报下需不需要一个起始时间
    var timegap: String = "",//上报间隔  分钟,数字    5-1440
)