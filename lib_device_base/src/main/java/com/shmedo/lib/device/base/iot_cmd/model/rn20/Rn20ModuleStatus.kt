package com.shmedo.lib.device.base.iot_cmd.model.rn20

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/11/22 <br></br>
 * 描述：     RN20模块状态信息
 */
class Rn20ModuleStatus {
    var sn //SN号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var flash //存储芯片
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var ads //电压采集
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var ble //蓝牙
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var lora //Lora模块
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var vm501 //振弦采集模块
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var adxl362 //加速度模块
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var mmc5883 //方位角模块
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var scl3300 //倾角模块
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var aht21 //温湿度模块
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var rtc //rtc时钟
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var ltc2945 //电流检测模块
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}