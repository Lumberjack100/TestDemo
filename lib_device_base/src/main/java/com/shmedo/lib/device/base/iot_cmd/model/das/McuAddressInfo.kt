package com.shmedo.lib.device.base.iot_cmd.model.das

import android.text.TextUtils

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/4/7 <br/>
 * 描述：    水利遥测设备地址信息
 */
class McuAddressInfo {
    var mcuaddr //MCU 地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}