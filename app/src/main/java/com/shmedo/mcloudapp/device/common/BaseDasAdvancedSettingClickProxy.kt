package com.shmedo.mcloudapp.device.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述： TODO
 */
open class BaseDasAdvancedSettingClickProxy : BaseClickProxy() {
    open fun onFirmWareSelectClick() {}

    open fun onSyncLocationClick() {}

    open fun onResetClick() {}

    open fun onAudibleAlarmClick() {}

    open fun onCommandDebugClick() {}

    open fun onRemoteDebuggingClick() {}

    override fun onSubmitButtonClick() {
        TODO("Not yet implemented")
    }
}