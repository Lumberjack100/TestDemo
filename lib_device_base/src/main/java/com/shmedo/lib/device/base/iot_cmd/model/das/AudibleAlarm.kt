package com.shmedo.lib.device.base.iot_cmd.model.das

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：     声光报警器信息
 */
class AudibleAlarm {
    var alarmstatus //声光报警器开启状态 0 关闭 1 开启
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "1" else field

    var screenstatus //电子点阵屏开启状态 0 关闭 1 开启
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "1" else field

    var alarmtype //类型  0  降雨量  1 水位
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field

    var alarmaddr //声光报警器地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var level1 //1级预警值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var level2 //2级预警值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var level3 //3级预警值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var playtime //播放时长 秒
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var playgap //播放间隙 秒
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var volume //音量大小
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field


    var screenaddr //电子屏地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var showtime //电子屏显示时长 秒
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var showgap //电子屏熄屏时长 秒
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    var mcuaddr //MCU 地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}