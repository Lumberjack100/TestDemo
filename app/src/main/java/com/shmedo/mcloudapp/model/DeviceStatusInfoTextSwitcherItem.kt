package com.shmedo.mcloudapp.model

import androidx.databinding.BaseObservable

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class DeviceStatusInfoTextSwitcherItem(
    val name: String = "",
    var value: String = "",
    var textColorRes: Int = 0,
    var isErrorInfo: Boolean = false,
    var isBottomItem: Boolean = false,

    ) : BaseObservable() {

    fun refreshValue(value: String, isErrorInfo: Boolean = false) {
        this.value = value
        this.isErrorInfo = isErrorInfo
        notifyChange()
    }

}