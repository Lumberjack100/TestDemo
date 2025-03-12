package com.shmedo.mcloudapp.model

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

        //偏移初始化
        data object OFFSET_INITIALIZATION : Type()

        //重启
        data object REBOOT : Type()

        //重置
        data object RESET : Type()

        //同步安装位置
        data object SYNC_INSTALL_POSITION : Type()

        //远程调试
        data object REMOTE_DEBUG : Type()

        //待机
        data object STANDBY : Type()
    }
}




