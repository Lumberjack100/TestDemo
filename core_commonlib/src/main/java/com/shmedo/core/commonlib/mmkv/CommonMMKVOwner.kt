package com.shmedo.core.commonlib.mmkv


/**
 * 创建者：gonghe
 * 创建时间：2024/3/26
 * 描述： TODO
 */
object CommonMMKVOwner : MMKVOwner(mmapID = "common_settings") {
    var isFirstOpenApp by mmkvBool(default = true)

    var isAgreePrivate by mmkvBool(default = false)

    var primaryColor by mmkvInt(default = 0)

    var appLogSessionId by mmkvString(default = "")//应用日志

    var iotDeviceLogSessionId by mmkvString(default = "")//物联网设备日志

    var isCommandDebugMode by mmkvBool(default = false)//指令下发模式

}
