package com.shmedo.lib.device.base.iot_cmd.model.lr200

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/2/25 <br></br>
 * 描述：     TODO
 */
class LR200PositionInfo {
    var lng: String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var lat: String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}