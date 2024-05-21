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

        //固件
        data object FIRMWARE : Type()

        //重置
        data object RESET : Type()

        //同步安装位置
        data object SYNC_INSTALL_POSITION : Type()
    }
}




