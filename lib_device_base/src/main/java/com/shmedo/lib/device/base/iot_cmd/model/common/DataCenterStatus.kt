package com.shmedo.lib.device.base.iot_cmd.model.common

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/29/20 <br></br>
 * 描述：     数据中心状态
 */
data class DataCenterStatus(
    var centerid: Int = 0,//数据中心编号
    var status: String = "",//0未开启，1已上线，2未上线
)