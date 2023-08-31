package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/13 <br></br>
 * 描述：      ADME 电压配置参数
 */
class AdmeVoltageConfigInfo {
    var volt_power_standard //驱动器标压阈值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var volt_power_low //驱动器低压阈值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var volt_power_under //驱动器欠压阈值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var volt_sensor_standard //测斜仪标压阈值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var volt_sensor_low //测斜仪低压阈值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var volt_sensor_under //测斜仪欠压阈值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var rope_length //钢丝绳长
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}