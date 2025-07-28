package com.shmedo.mcloudapp.model

import androidx.databinding.BaseObservable
import com.shmedo.mcloudapp.R

/**
 * 创建者：gonghe
 * 创建时间：2024/6/6
 * 描述： TODO
 */
data class ExternalDigitalSensorParamEditItem(
    val name: String = "",
    var value: String = "",
    val tipDesc: String = "",
    val inputHint: String = "请输入",
    val inputTypeFilter: String = "numberDecimal",
    val inputLengthFilter: Int = 10,
    val inputEnable: Boolean = true,
    val btnVisible: Boolean = false,
    val btnText: String = "重置",
    val bgResId: Int = R.drawable.layer_common_click_item_with_divider,
) : BaseObservable() {

    fun refreshValue(value: String) {
        this.value = value
        notifyChange()
    }
}
