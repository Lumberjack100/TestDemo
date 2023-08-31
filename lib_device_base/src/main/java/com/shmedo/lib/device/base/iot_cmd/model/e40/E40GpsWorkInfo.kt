package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/6/18 <br></br>
 * 描述：     E40 GPS 工作参数
 */
class E40GpsWorkInfo {
    var cutoffangle //卫星仰角截止角 范围0-90度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var range //观测范围  0或1
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var savefreq //数据频率
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}