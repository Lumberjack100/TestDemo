package com.shmedo.lib.device.base.iot_cmd.model.m20

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/27/21 <br></br>
 * 描述：     M20的基本信息
 */
class M20BaseInfo {
    var sn: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var productid //产品型号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var firversion //固件版本
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}