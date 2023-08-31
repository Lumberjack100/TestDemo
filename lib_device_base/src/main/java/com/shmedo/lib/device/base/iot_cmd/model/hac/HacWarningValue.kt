package com.shmedo.lib.device.base.iot_cmd.model.hac

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/20 <br></br>
 * 描述：     预警值
 */
class HacWarningValue {
    var x1min //一级预警 X 轴最小值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var x1max //一级预警 X 轴最大值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var y1min //一级预警 Y 轴最小值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var y1max //一级预警 Y 轴最大值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var x2min //二级预警 X 轴最小值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var x2max //二级预警 X 轴最大值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var y2min //二级预警 Y 轴最小值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var y2max //二级预警 Y 轴最大值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var x3min //三级预警 X 轴最小值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var x3max //三级预警 X 轴最大值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var y3min //三级预警 Y 轴最小值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var y3max //三级预警 Y 轴最大值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}