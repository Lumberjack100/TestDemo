package com.shmedo.lib.device.base.iot_cmd.model.e40

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2/26/21 <br></br>
 * 描述：     E40 CORS 服务参数信息
 */
class E40CORSInfo {
    var sw //0：表示关闭，1：表示打开
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var addr //服务器地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var port //服务器端口号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var user //用户名
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var pswd //密码
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var sta //站点名
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}