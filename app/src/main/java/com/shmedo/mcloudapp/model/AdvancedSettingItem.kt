package com.shmedo.mcloudapp.model

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class AdvancedSettingItem(
    val name: String = "",
    val type: Type = Type.FIRMWARE_UPGRADE
) {
    sealed class Type {

        //固件升级
        data object FIRMWARE_UPGRADE : Type()

        //偏移初始化
        data object OFFSET_INITIALIZATION : Type()

        //重启
        data object REBOOT : Type()

        //休眠
        data object HIBERNATION : Type()

        //重置
        data object RESET : Type()

        //同步安装位置
        data object SYNC_INSTALL_POSITION : Type()

        //远程调试
        data object REMOTE_DEBUG : Type()

        //待机
        data object STANDBY : Type()

        //监测数据导出
        data object MONITORING_DATA_EXPORT : Type()

        //格式化数据存储
        data object FORMAT_DATA_STORAGE : Type()

        //更换设备
        data object REPLACE_DEVICE : Type()

        //一键配置
        data object QUICK_CONFIG : Type()
    }
}




