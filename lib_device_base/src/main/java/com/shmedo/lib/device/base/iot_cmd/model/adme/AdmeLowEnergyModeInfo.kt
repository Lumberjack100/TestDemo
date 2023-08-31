package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：      ADME 低功耗模式
 */
class AdmeLowEnergyModeInfo {
    var mode //工作模式(0:正常模式，1:低功耗模式)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}