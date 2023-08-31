package com.shmedo.lib.device.base.iot_cmd.model.rn20

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/8/3 <br></br>
 * 描述：     TODO
 */
class Rn20BaseInfo {
    var sn //SN号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var ver //固件版本
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var local //位置
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var involt //内部电量
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var ssi //信号强度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var recvbuf //接收数据包数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var sendbuf //发送数据包数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var netid //网络号
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var addr //终端地址
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var channel //通讯信道
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var finaltime //最后交互时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var logintime //终端注册时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}