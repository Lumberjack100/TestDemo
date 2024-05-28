package com.shmedo.mcloudapp.device.model

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     TODO
 */
data class MRDODIPortItem(
    var isOpen: Boolean = false, //是否 开启
    var ktype: String = "",//数字  1-8
    val name: String = "",//名称
    var isSwitchButtonVisible: Boolean = true, //是否显示开关按钮
)