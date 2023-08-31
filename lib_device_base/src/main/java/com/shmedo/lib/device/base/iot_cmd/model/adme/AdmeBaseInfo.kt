package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/23/20 <br></br>
 * 描述：     ADME的基本信息
 */
class AdmeBaseInfo {
    var sn: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var productid //产品型号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var equimodel //设备模式（0：设备配置模式，1：自动检测模式）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var online //在线状态（0：离线，非0:在线)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}