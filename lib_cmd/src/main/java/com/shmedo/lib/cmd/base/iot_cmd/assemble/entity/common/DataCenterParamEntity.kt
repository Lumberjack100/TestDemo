package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/8 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class DataCenterParamEntity(
    val centerid: String = "",
    var protocol: String = IOTConstants.NULL_KEY, //传输协议;TCP-C/TCP-S/MQTT
    val datatype: String = IOTConstants.NULL_KEY, //数据协议
    val plattype: String = IOTConstants.NULL_KEY, //平台类型
    val addr: String = "",//数据中心地址
    val port: String = "", //数据中心端口

    /**
     * MQTT 协议特有配置参数
     */
    var projid: String = IOTConstants.NULL_KEY, //产品ID(MQTT参数)
    var deviceid: String = IOTConstants.NULL_KEY, //设备id（MQTT参数）,设备id、key设置为空时，设备通过自动注册的方式获取id、key
    var devicekey: String = IOTConstants.NULL_KEY,//设备key（MQTT参数）
    var regcode: String = IOTConstants.NULL_KEY, //产品注册码（MQTT参数）
    var httpaddr: String = IOTConstants.NULL_KEY, //设备注册HTTP地址，域名或者IP（MQTT参数）,MQTT协议下，设备通过自动注册的方式获取到设备id，key
    var httpport: String = IOTConstants.NULL_KEY, //设备注册HTTP端口（MQTT参数）

    /**
     * SL651水文协议特有配置参数
     */
    var type_code: String = IOTConstants.NULL_KEY, //测站分类编码
    var co_address: String = IOTConstants.NULL_KEY, //中心站地址  0-256
    var password: String = IOTConstants.NULL_KEY, //密码 0-65535
    var taddress: String = IOTConstants.NULL_KEY, //遥测站地址   10个数字
//    var timed_report: String = IOTConstants.NULL_KEY, //定时报开关  0:关闭 1:开启
    var hour_report: String = IOTConstants.NULL_KEY,//小时报开关  0:关闭 1:开启
//    var add_report: String = IOTConstants.NULL_KEY,//加报报开关  0:关闭 1:开启
    var data_link: String = IOTConstants.NULL_KEY, //数据链路维持报  0|[10,40]   0:关闭
    var valid_day: String = IOTConstants.NULL_KEY, //补发数据有效天数 1-180
    var reissue_time: String = IOTConstants.NULL_KEY, //数据补发间隔 (min)
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
