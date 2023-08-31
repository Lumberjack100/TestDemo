package com.shmedo.lib.device.base.iot_cmd.model.das

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  4/12/21 <br></br>
 * 描述：     DAS 采集器参数
 */
class DasCollectorInfo {
    var type //采集器型号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var addr //采集器地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var collgap //采集间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var calcgap //解算间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var standbygap //待机时长
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var sensornum //接入传感器个数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var sensitivity //灵敏度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}