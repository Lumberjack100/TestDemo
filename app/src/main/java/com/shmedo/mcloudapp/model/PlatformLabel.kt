package com.shmedo.mcloudapp.model

import androidx.databinding.BaseObservable

data class PlatformLabel(
    var content: String,
    var textColorRes: Int = 0,
    var bgResId: Int = 0
) : BaseObservable() {

    fun refreshValue(content: String, textColorRes: Int = 0, bgResId: Int = 0) {
        this.content = content
        this.textColorRes = textColorRes
        this.bgResId = bgResId
        notifyChange()
    }
}