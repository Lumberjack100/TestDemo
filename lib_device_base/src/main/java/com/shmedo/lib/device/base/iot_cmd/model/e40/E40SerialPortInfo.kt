package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/18 <br></br>
 * 描述：      E40 串口参数信息
 */
class E40SerialPortInfo {
    var type //输出数据格式
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var baud //波特率
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var databits //
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var parity: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var stopbits: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}