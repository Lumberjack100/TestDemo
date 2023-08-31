package com.shmedo.lib.device.base.iot_cmd.model.hac

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/22 <br></br>
 * 描述：     ADME AC10 孔深测量配置参数
 */
class HacMeasuringHoleDepthInfo {
    var address //MAC 地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var lowtbtss //下放堵转检测（0:关闭，1:开启）
            : String? = null
    var holelist: List<HacHoleAreaDepthInfo>? = null
        get() = if (field == null) ArrayList() else field
}