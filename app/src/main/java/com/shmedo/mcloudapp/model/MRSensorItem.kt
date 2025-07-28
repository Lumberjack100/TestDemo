package com.shmedo.mcloudapp.model

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     TODO
 */
@Parcelize
data class MRSensorItem(
    var isPlugin: Boolean = false, //是否接入
    var chl: String = "",//通道号
    var addr: String = "",//地址
    var addrDesc: String = "",//地址 or 通道号说明
    val sensorID: String = "",//传感器类型标识
    val sensorName: String = "",//传感器名称
    val modelToken: String = "",//物模型
    var isShowDel: Boolean = true,//是否显示删除按钮
    val uuid: String = ""
) : Parcelable, BaseObservable() {

    fun refreshStatus(isPlugin: Boolean, addr: String, addrDesc: String) {
        this.isPlugin = isPlugin
        this.addr = addr
        this.addrDesc = addrDesc
        notifyChange()
    }
}