package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/7/4 <br></br>
 * 描述：     NMEA输出内容及输出频率
 */
class E40NmeaTimeInfo {
    var gga: String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var rmc: String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var vtg: String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var gsv: String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var gsa: String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}