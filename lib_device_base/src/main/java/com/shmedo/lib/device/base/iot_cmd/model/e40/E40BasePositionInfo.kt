package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/7/2 <br></br>
 * 描述：     E40基站位置信息
 */
class E40BasePositionInfo {
    var mode //1表示自动模式，2表示第一次自动获取以后采用第一次值，3表示手动模式，当为自动模式时，可不设置其他参数。
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "1" else field
    var lon //经度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var lat //纬度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var alt //高程
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}