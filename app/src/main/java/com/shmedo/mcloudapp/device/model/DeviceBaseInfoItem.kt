package com.shmedo.mcloudapp.device.model

import androidx.databinding.BaseObservable

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class DeviceBaseInfoItem(
    val name: String = "",
    var value: String = "",
    ) : BaseObservable() {

    fun refreshValue(value: String) {
        this.value = value
        notifyChange()
    }
}