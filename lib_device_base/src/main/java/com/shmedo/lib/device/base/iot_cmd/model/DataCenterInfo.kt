package com.shmedo.lib.device.base.iot_cmd.model

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/18/20 <br></br>
 * 描述：     数据中心参数信息
 */
class DataCenterInfo {
    var centerid: String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var protocol //传输协议;TCP-C/TCP-S/MQTT
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var datatype //数据协议,由设备类型决定
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var plattype //平台类型
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "0" else field
    var addr //数据中心地址,addr和port设置为空时，关闭该数据中心
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var port //数据中心端口
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    /**
     * MQTT 协议特有配置参数
     */
    var deviceid //设备id（MQTT参数）,设备id、key设置为空时，设备通过自动注册的方式获取id、key
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var devicekey //设备key（MQTT参数）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var httpaddr //设备注册HTTP地址，域名或者IP（MQTT参数）,MQTT协议下，设备通过自动注册的方式获取到设备id，key
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var httpport //设备注册HTTP端口（MQTT参数）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var projid //产品ID(MQTT参数)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var regcode //厂商设备注册码（MQTT参数）
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    /**
     * SL651水文协议特有配置参数
     */
    var type_code //测站编码
            : String? = null
    var co_address //中心站地址
            : String? = null
    var password //密码
            : String? = null
    var taddress //遥测站地址
            : String? = null
    var hour_report //小时报开启标识  1:开启 0:关闭
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var data_link //数据链路维持报  0|[10,40]   0:关闭
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var valid_day //补发数据有效天数
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var reissue_time //数据补发间隔
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}