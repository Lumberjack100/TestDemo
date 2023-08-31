package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/1/21 <br></br>
 * 描述：     E40 板卡解算参数信息
 */
class E40BoardSolutionInfo {
    var inittime //初始化时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var calcgap //解算时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var smoothlevel //平滑等级
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var reinit //重新初始化
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var rtkdynamicmode //RTK动态模式
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var corrval //形变修正值
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}