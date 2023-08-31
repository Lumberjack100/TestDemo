package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2/25/21 <br></br>
 * 描述：     GNSS RTK 模式
 */
class E40RTKModeInfo {
    var mode //0表示基站，1表示移动站
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}