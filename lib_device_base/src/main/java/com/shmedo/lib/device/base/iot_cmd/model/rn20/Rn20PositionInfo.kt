package com.shmedo.lib.device.base.iot_cmd.model.rn20

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/8/4 <br></br>
 * 描述：     TODO
 */
class Rn20PositionInfo {
    var longitude: String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var latitude: String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}