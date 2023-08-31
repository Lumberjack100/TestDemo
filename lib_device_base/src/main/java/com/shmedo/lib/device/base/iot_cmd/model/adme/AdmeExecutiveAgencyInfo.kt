package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/29/20 <br></br>
 * 描述：     ADME 执行机构参数
 */
class AdmeExecutiveAgencyInfo {
    var meastype //测量方式（0:实时测量，1:整时整点测量，2:定时定点测量）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var datatype //数据解算方式（0:顶部固定法，1底部固定法）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var datareply //数据应答（0:关闭，1:启用）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var roundwaitetime //每轮等待时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var roundmeasinval //每轮测量间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var updatedate // 修改日期
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var invalday //间隔天数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var roundmeasstart //每轮测量开始时间
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
    var interdeep //测斜管孔深
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
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
    var meabaseth //测量基准深度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var dwonblocked //下放堵转预判（0:关闭，1:开启）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var untimenum //堵转单位时间脉冲数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var detectiontime //堵转检测判断时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var detectionstart //堵转检测起点
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var detectionend //堵转检测终点
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var interval_compensation //管口安全距离 h1
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var bottom_safe_distance //管底安全距离
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var interval_fitting //数据拟合区间h2
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var point_offset //测点下移距离 h3
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}