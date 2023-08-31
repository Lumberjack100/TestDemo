package com.shmedo.lib.device.base.iot_cmd.model.das

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/19 <br></br>
 * 描述：     开关量传感器参数
 */
class DasIOSensorInfo {
    var type //0：关闭开关量功能 1：雨量站模式 2：断线报警器模式
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var value //当type取1时，value代表雨量计精度  当type取2时，value代表断线报警器状态，0：常开，1：常关
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var min_time //雨量计翻斗翻转最小间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
}