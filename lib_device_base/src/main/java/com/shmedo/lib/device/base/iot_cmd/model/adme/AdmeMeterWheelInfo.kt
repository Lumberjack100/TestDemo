package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/27/20 <br></br>
 * 描述：    ADME 计米轮配置参数
 */
class AdmeMeterWheelInfo {
    var enclinenum //编码器线数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var outline //外径
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var uptiona //上拉一次修正参数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var uptionb //上拉二次修正参数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var upconstant //上拉常数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var upfilter //上拉滤波器系数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downtiona //下放一次修正参数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downtionb //下放二次修正参数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downconstant //下放常数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downfilter //下放滤波器系数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}