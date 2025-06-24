package com.shmedo.lib.cmd.base.iot_cmd.model.common

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/29/20 <br></br>
 * 描述：     数据链路状态
 */
data class DataCenterStatus(
    var centerid: Int = 0,//数据链路编号
    var status: String = "",//0 未启用，1 已上线，2 未上线
)