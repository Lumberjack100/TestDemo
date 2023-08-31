package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/24/20 <br></br>
 * 描述：    ADME 基础配置参数
 */
class AdmeBasicConfigInfo {
    var inctype //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var address //采集器 / MAC 地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var interdeep //测斜管孔深
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downspeed //下放速度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var downwaitetime //下放等待时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var datatype //数据解算方式（0:顶部固定法，1底部固定法）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}