package com.shmedo.lib.device.base.iot_cmd.entity.adme

import android.text.TextUtils
import com.shmedo.lib.device.base.Validater
import com.shmedo.lib.device.base.exception.DASParameterException


/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/1/25 <br></br>
 * 描述：    ADME 拟人运动控制
 */
class AdmeAnthropomorphicMovementEntity : Validater {
    var mode: String? = null
    override fun validate() {
        if (TextUtils.isEmpty(mode)) throw DASParameterException("ADME 拟人运动使能参数不正确")
    }

    override fun toString(): String {
        return "mode=$mode"
    }
}