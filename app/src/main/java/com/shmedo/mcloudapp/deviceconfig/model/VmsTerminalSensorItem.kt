package com.shmedo.mcloudapp.deviceconfig.model

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/23/20 <br></br>
 * 描述：     Vms 终端传感器
 */
class VmsTerminalSensorItem {
    var channel //传感器所在通道
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var num //传感器编号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var resId = 0

    var isInsert = false //接入判断，0：未接入，1：接入
}