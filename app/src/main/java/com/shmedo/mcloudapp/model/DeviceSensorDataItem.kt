package com.shmedo.mcloudapp.model

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class DeviceSensorDataItem(
    val time: String = "",
    val monitorName: String = "",
    val monitorValue: String = "",
    val isOpt: Boolean = false,
)