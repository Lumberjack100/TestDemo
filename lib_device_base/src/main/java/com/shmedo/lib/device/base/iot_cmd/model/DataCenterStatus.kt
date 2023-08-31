package com.shmedo.lib.device.base.iot_cmd.model

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/29/20 <br></br>
 * 描述：     数据中心状态
 */
class DataCenterStatus {
    var centerid //数据中心编号
            = 0
    var status //0未开启，1已上线，2未上线
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}