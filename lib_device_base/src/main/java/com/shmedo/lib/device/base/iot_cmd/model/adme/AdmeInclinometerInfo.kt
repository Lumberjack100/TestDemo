package com.shmedo.lib.device.base.iot_cmd.model.adme

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
    @Deprecated("此字段已废弃，没有实际业务意义", replaceWith = ReplaceWith("用 mode 表示测量工作模式"))
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
    var mode //测量工作模式(5:蓝牙关测量关，7:蓝牙开测量关，8:蓝牙关测量开，9:蓝牙开测量开)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "5" else field

    var incversion //测斜仪版本（0：2.1 版本，1：3.0 版本）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var compenway //补偿方式（0：X+Y轴无扭转角补偿，1：X轴扭转角补偿）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var torangle //扭转角γ
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}