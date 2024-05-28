package com.shmedo.mcloudapp.device.model

import androidx.databinding.BaseObservable

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class FilterDeviceTabItem(
    var name: String = "",
    var value: String = "",
    var isShowDropDown: Boolean = true,
    var singleSelectionItemLastSelectedIndex: Int = 0,
) : BaseObservable() {

    fun refreshValue(value: String, index: Int = 0) {
        this.value = value
        this.singleSelectionItemLastSelectedIndex = index
        notifyChange()
    }
}