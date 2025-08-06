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
    var displayValue: String = "",
    var cmdValue: String = "",
    val fieldValueInfos: MutableList<FieldValueInfo> = mutableListOf(),
    val bgResId: Int = R.drawable.layer_common_click_item_with_divider
) : BaseObservable() {

    fun refreshValue(displayValue: String, cmdValue: String) {
        this.displayValue = displayValue
        this.cmdValue = cmdValue
        notifyChange()
    }
}