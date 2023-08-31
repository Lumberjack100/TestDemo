package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/20 <br></br>
 * 描述：     TODO
 */
class SolarBean {
    var sw: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var status = false
    var sloarvolt = 0.0
    var batvolt = 0.0
    var payloadvolt = 0.0
}