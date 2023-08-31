package com.shmedo.lib.device.base.iot_cmd.model.hac

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/29/20 <br></br>
 * 描述：     ADME 执行机构参数
 */
class HacExecutiveAgencyInfo {
    var datatype //数据解算方式（0:顶部固定法，1底部固定法）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var datareply //数据应答（0:关闭，1:启用）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var datainval //数据读取间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var compensatetime //测量补偿时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var driveaddress //电机驱动器地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downspeed //电机下放速度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downwaitetime //下放等待时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var upspeed //电机上拉速度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var measpacing //测量间距
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var meaintertime //测量间隔时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var interval_compensation //距离补偿区间h1
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var interval_fitting //数据拟合区间h2
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var point_offset //测点偏移距离h3
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}