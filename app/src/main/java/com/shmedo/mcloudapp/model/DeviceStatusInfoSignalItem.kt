package com.shmedo.mcloudapp.model

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class DeviceStatusInfoSignalItem(
    val name: String = "",
    var signalValue: Int = 0,
    var textColorRes: Int = 0,
    var isBottomItem: Boolean = false,
)