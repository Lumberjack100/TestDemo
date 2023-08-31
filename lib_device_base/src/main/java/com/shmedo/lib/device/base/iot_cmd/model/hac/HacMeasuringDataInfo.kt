package com.shmedo.lib.device.base.iot_cmd.model.hac

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/11 <br></br>
 * 描述：     ADME AC10 数据测量配置参数
 */
class HacMeasuringDataInfo {
    var equipmodel //电机工作标识 0：停止 1：开始测量 2: 异常
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var address //MAC 地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downwaitetime //下放等待时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var datatype //数据解算方式（0:顶部固定法，1底部固定法）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var onewaytest //单向测量 0 :关闭 1:开启
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var holelist: List<HacHoleAreaDepthInfo>? = null
        get() = if (field == null) ArrayList() else field
    var checkreverse //反转自检
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}