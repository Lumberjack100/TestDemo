package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/2/21 <br></br>
 * 描述：     ADME的电机运动堵转缓停参数
 */
class AdmeLockedRotorDetectionInfo {
    var lowtbtss //下放堵转缓停（0:关闭，1:开启）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var numpput //单位时间脉冲数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var pdajtime //脉冲检测判断时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var detintiona //堵转检测区间起始值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var detintionb //堵转检测区间终值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var lowtorblothr //下放力矩堵转阈值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var lowtordetime //下放力矩检测判断时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var lowsusrana //下放缓停区间起始值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var lowsusranb //下放缓起区间终值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var uptbtss //上拉堵转缓停（0:关闭，1:开启）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var uptorblothr //上拉力矩堵转阈值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var uptordetime //上拉力矩检测判断时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var upsusrana //上拉缓停区间起始值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var upsusranb //上拉缓起区间终值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var holedepth //下放距离
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field

    var measpacing //上拉测量间距
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}