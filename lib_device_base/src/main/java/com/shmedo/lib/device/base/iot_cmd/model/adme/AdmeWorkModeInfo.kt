package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：      ADME 工作模式
 */
class AdmeWorkModeInfo {
    var workmode //工作模式(0:常规测量模式，1:特定点位模式，2:静态测量模式，3:设备停用模式)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}