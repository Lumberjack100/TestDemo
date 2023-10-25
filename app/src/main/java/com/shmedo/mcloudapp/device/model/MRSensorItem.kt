package com.shmedo.mcloudapp.device.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     TODO
 */
@Parcelize
data class MRSensorItem(
    val isPlugin: Boolean = false, //是否接入
    val sensorType: String = "",//传感器类型
    val sensorName: String = "",//传感器名称
    val modelToken: String = "",//物模型
    val chl: String = "",//通道号
    val addr: String = "",//地址
    val addrDesc: String = "",//地址 or 通道号说明
    var isShowDel: Boolean = false,//是否显示删除按钮
) : Parcelable