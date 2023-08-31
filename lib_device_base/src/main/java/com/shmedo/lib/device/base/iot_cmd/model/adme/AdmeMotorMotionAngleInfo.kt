package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/8/21 <br></br>
 * 描述：      ADME电机实时运动数据
 */
class AdmeMotorMotionAngleInfo {
    var pulsenumber //脉冲数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var realmoveangle // 实时运动角度
            : String? = null
}