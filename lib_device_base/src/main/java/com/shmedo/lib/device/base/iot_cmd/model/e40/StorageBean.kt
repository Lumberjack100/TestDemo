package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/20 <br></br>
 * 描述：     TODO
 */
class StorageBean {
    var ram: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var flash: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var tfcard: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}