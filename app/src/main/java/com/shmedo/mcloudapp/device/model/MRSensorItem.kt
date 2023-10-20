package com.shmedo.mcloudapp.device.model

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     TODO
 */
data class MRSensorItem(
    val isPlugin: Boolean = false, //是否接入
    val name: String = "",//名称
    val model: String = "",//物模型
    val addr: String = "",//地址 or 通道号
    var isShowDel: Boolean = false,//是否显示删除按钮
)