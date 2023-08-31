package com.shmedo.lib.device.base.iot_cmd.model.hac

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/11 <br></br>
 * 描述：     孔号、区号、孔深对应实体类
 */
class HacHoleAreaDepthInfo {
    var holeno //孔号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var areano //区号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var holedepth // 孔深
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var measdepth // 孔深
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}