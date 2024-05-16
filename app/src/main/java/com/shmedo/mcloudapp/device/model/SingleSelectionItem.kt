package com.shmedo.mcloudapp.device.model

import androidx.databinding.BaseObservable
import com.blankj.utilcode.util.ColorUtils
import com.shmedo.mcloudapp.R

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class SingleSelectionItem(
    var name: String = "",
    var extValue: String = "",
    var isChecked: Boolean = false,
    var checkedTextColorRes: Int = 0,
) : BaseObservable() {

    fun refreshName(value: String) {
        this.name = value
        notifyChange()
    }

    fun refreshChecked(isSelect: Boolean) {
        this.isChecked = isSelect
        notifyChange()
    }

    fun obtainCheckedTextColorRes(): Int {
        return if (checkedTextColorRes == 0) ColorUtils.getColor(R.color.colorPrimary) else checkedTextColorRes
    }

}