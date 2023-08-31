package com.shmedo.lib.device.base.iot_cmd.model.das

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：  DAS 状态页面MEMS传感器状态
 */
class MemsBean {
    var type = 0
    var vaule: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var errno = 0
}