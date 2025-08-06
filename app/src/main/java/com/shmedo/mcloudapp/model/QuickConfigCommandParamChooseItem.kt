package com.shmedo.mcloudapp.model

import androidx.databinding.BaseObservable
import com.shmedo.core.model.FieldValueInfo
import com.shmedo.mcloudapp.R

/**
 * 创建者：gonghe
 * 创建时间：2024/6/6
 * 描述： TODO
 */
data class QuickConfigCommandParamChooseItem(
    val cmdChnName: String = "",
    val cmdEngName: String = "",
    var value: String = "",
    var defaultValue: String = "",
    val fieldValueInfos: MutableList<FieldValueInfo> = arrayListOf(),
    val bgResId: Int = R.drawable.layer_common_click_item_with_divider
) : BaseObservable() {

    fun refreshValue(value: String) {
        this.value = value
        notifyChange()
    }
}