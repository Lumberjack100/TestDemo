package com.shmedo.mcloudapp.device.model

import androidx.databinding.BaseObservable

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class DeviceStatusInfoBasicItem(
    val name: String = "",
    var value: String = "",
    var textColorRes: Int = 0,
    var isClickable: Boolean = false,
    var isClipboard: Boolean = false

) : BaseObservable() {

    fun refreshValue(value: String) {
        this.value = value
        notifyChange()
    }

    fun refreshClipboardState(value: Boolean) {
        this.isClipboard = value
        notifyChange()
    }
}