package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/1/21 <br></br>
 * 描述：    E40 有线网络参数信息
 */
class E40EthernetInfo {
    var dhcp: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var ip: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var netmask //子网掩码
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var gateway: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var dns: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}