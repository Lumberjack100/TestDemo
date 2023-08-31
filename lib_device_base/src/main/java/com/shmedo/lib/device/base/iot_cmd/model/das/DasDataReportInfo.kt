package com.shmedo.lib.device.base.iot_cmd.model.das

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/20 <br></br>
 * 描述：     数据上报时间
 */
class DasDataReportInfo {
    var report_intv //数据上报间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var plus_intv //加报间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var plus_count //加报次数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}