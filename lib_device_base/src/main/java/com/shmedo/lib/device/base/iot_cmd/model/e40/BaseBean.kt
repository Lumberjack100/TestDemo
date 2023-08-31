package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/20 <br></br>
 * 描述：     TODO
 */
class BaseBean {
    var sn: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var iccid: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var imei: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var version: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var oem: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var volt = 0.0
}