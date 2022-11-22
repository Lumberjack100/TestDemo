package com.shmedo.configlibrary.iot.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：     ADME 测斜仪配置参数
 */
class AdmeInclinometerInfo {
    var inctype //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var lowpower //低功耗模式(0:关闭，1:开启)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var address //采集器 / MAC 地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var collinval //采集器采集间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var calcinval //采集器解算间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var dormancytime //休眠时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var interupdate //测斜仪修正值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}