package com.shmedo.lib.device.base.iot_cmd.model.hac

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/25 <br></br>
 * 描述：     HAC 电机实时运动脉冲数据
 */
class HacMotorMotionDistanceInfo {
    var pulsenumber //脉冲数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var realmovedistance //实时运动距离
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var realholedepth // 实时测量孔深
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var abndiasis // 设备异常诊断
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}