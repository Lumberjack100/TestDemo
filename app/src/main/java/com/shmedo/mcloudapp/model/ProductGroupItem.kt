package com.shmedo.mcloudapp.model

import androidx.databinding.BaseObservable

/**
 * 创建者：gonghe
 * 创建时间：2025/6/23
 * 描述： TODO
 */
data class ProductGroupItem(
    val children: MutableList<ProductSeriesItem> = arrayListOf(),
)

data class ProductSeriesItem(
    var name: String = "",
    val productIdList: MutableList<Int> = arrayListOf(),
    var checked: Boolean = false,
) : BaseObservable() {

    fun refreshName(value: String) {
        this.name = value
        notifyChange()
    }

    fun refreshChecked(isSelect: Boolean) {
        this.checked = isSelect
        notifyChange()
    }

}
