package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/8 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRDataCenterParamEntity(
    val centerid: String = "",
    val switch: String = "",//开启状态 0:关闭 1:开启
    val line: String = "",//通信线路选择  1：4G 2：有线
    val level: String = "",//网络协议 1:ipv4 2:ipv6
    val type: String = "", //协议类型 1:TCP 2:UDP
    val addr: String = "",//数据中心地址
    val port: String = "", //数据中心端口
    val plattype: String = "", //平台类型 DIDA:地大平台  ZYWL:成都理工平台  GHIOT:米度平台 DIDA2:地大平台2 HWPEC:河南水利 MDSW:米度水文
    val datatype: String = "", //数据协议,1：TCP-C 2:MQTT 3:SL651水文协议

    /**
     * MQTT 协议特有配置参数
     */
    var projid: String = IOTConstants.NULL_KEY, //产品ID(MQTT参数)
    var deviceid: String = IOTConstants.NULL_KEY, //设备id（MQTT参数）,设备id、key设置为空时，设备通过自动注册的方式获取id、key
    var devicekey: String =IOTConstants.NULL_KEY,//设备key（MQTT参数）
    var regcode: String = IOTConstants.NULL_KEY, //产品注册码（MQTT参数）
    var httpaddr: String = IOTConstants.NULL_KEY, //设备注册HTTP地址，域名或者IP（MQTT参数）,MQTT协议下，设备通过自动注册的方式获取到设备id，key
    var httpport: String = IOTConstants.NULL_KEY, //设备注册HTTP端口（MQTT参数）
    var keepalive: String = IOTConstants.NULL_KEY, //心跳间隔(MQTT)     链路维持报间隔(SL651   0关闭)

    /**
     * SL651水文协议特有配置参数
     */
    var type_code: String = IOTConstants.NULL_KEY, //测站分类编码
    var co_address: String = IOTConstants.NULL_KEY, //中心站地址  0-256
    var password: String = IOTConstants.NULL_KEY, //密码 0-65535
    var taddress: String = IOTConstants.NULL_KEY, //遥测站地址   10个数字
    var timed_report: String = IOTConstants.NULL_KEY, //定时报开关  0:关闭 1:开启
    var hour_report: String = IOTConstants.NULL_KEY,//小时报开关  0:关闭 1:开启
    var add_report: String = IOTConstants.NULL_KEY,//加报报开关  0:关闭 1:开启
    var maintain_report: String = IOTConstants.NULL_KEY,//维持报开关  0:关闭 1:开启
    var valid_day: String = IOTConstants.NULL_KEY, //补发数据有效天数 1-180
    var reissue_time: String = IOTConstants.NULL_KEY, //数据补发间隔 (min)
){
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}
