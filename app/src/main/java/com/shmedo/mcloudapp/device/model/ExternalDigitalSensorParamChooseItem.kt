package com.shmedo.mcloudapp.device.model

import androidx.databinding.BaseObservable

/**
 * 创建者：gonghe
 * 创建时间：2024/6/6
 * 描述： TODO
 */
data class ExternalDigitalSensorParamChooseItem(
    val name: String = "",
    var value: String = "",
): BaseObservable() {

    fun refreshValue(value: String) {
        this.value = value
        notifyChange()
    }
}