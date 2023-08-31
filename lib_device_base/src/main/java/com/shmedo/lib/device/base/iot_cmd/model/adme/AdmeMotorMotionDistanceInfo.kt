package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/7/21 <br></br>
 * 描述：     ADME电机实时运动数据
 */
class AdmeMotorMotionDistanceInfo {
    var pulsenumber //脉冲数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var realmovedistance //实时运动距离
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var realholedepth // 实时测量孔深
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var recoholedepth //  推荐测量孔深
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var realmoveangle // 实时运动角度
            : String? = null
}