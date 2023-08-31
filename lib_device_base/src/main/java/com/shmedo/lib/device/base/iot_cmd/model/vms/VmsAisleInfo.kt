package com.shmedo.lib.device.base.iot_cmd.model.vms

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/15/20 <br></br>
 * 描述：   Vms网关通道的控制参数信息
 */
class VmsAisleInfo {
    var channel //通道号
            = 0
    var netid //网关配置通道的网络号,取值[1,65535]，默认为1
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var ppt //空中唤醒时间，取值[0,5]s，默认2，当为0时，lora模块不休眠
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var addr //网关配置通道的地址,取值[0,63]，channel=0，默认为1；channel=1，默认为2；channel=2，默认为3
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var chl //通信信道，取值[0,31]，channel=0，默认为23；channel=1，默认为20；channel=2，默认为26
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var terminalmode //终端工作模式，取值[0,1],0低功耗模式，1正常模式
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var sendgap //指令发送间隔，取值≥3s，默认为3
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var offline //存活时间（终端长时间无数据，网关将终端删除），取值≥7200s，默认43200s
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var sleepgap //终端休眠时间，取值[0,5]s，默认为2
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var wakeupgap //终端唤醒时间，取值[0,65535]ms，默认100
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var airbaud //空中速率，取值[1~6]
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var terminalnum //终端接入数量
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var rssi //信号强度
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}