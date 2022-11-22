package com.shmedo.configlibrary.iot.cmd.entity.adme

import android.text.TextUtils
import com.shmedo.configlibrary.ble.exception.DASParameterException
import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/24/20 <br></br>
 * 描述：     ADME 低功耗模式
 */
class AdmeLowEnergyModelEntity : Validater {
    var mode: String? = null
    override fun validate() {
        if (TextUtils.isEmpty(mode)) throw DASParameterException("ADME 模式不正确")
    }

    override fun toString(): String {
        return "mode=$mode"
    }
}