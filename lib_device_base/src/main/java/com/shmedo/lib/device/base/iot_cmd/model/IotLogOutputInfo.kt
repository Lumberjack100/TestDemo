package com.shmedo.lib.device.base.iot_cmd.model

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/8/11 <br></br>
 * 描述：     日志输出方式
 */
class IotLogOutputInfo {
    var level: String=""
        get() = if (TextUtils.isEmpty(field)) "off" else field
    var type: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}