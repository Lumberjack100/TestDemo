package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：    ADME 步进电机参数
 */
class AdmeStepperMotorInfo {
    var posnegtest //正反测（0:关闭，1:开启）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var absprsion //绝对精度修正值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var movspeed //步进电机运动速度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var movesm //步进电机力矩
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}