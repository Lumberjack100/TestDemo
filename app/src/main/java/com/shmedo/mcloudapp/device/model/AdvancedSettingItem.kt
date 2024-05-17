package com.shmedo.mcloudapp.device.model

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class AdvancedSettingItem(
    val name: String = "",
    val type: Type = Type.FIRMWARE
) {
    sealed class Type {
        data object FIRMWARE : Type()

        data object RESET : Type()
    }
}




