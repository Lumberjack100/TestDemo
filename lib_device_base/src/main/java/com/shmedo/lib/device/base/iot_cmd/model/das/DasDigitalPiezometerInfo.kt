package com.shmedo.lib.device.base.iot_cmd.model.das

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/19 <br></br>
 * 描述：    数字水位计参数
 */
class DasDigitalPiezometerInfo {
    var sw //0：关闭数字水位计采集功能 1：打开数字水位计采集功能
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var addr //地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var threshold //触发阈值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var corrval //修正值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var ropelen //绳长（渗压计到管口的距离）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var tubealti //安装高程
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}