package com.shmedo.lib.device.base.iot_cmd.model.vms

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/11/12 <br></br>
 * 描述：    Vms网关的基本信息
 */
class VmsBasicInfo {
    var sn: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var swVersion //固件版本号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var volt //供电电压
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var online //在线状态（0：离线，非0:在线)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}