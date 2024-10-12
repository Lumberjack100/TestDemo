package com.shmedo.lib.cmd.base.iot_cmd.model.common

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/18/20 <br></br>
 * 描述：     数据链路参数信息
 */
data class DataCenterInfo(
    var centerid: String = "",
    var protocol: String = "", //传输协议;TCP-C/TCP-S/MQTT
    var datatype: String = "",//数据协议,由设备类型决定
    var plattype: String = "", //平台类型
    var addr: String = "", //数据链路地址,addr和port设置为空时，关闭该数据链路
    var port: String = "", //数据链路端口

    /**
     * MQTT 协议特有配置参数
     */
    var deviceid: String = "",//设备id（MQTT参数）,设备id、key设置为空时，设备通过自动注册的方式获取id、key
    var devicekey: String = "",//设备key（MQTT参数）
    var httpaddr: String = "", //设备注册HTTP地址，域名或者IP（MQTT参数）,MQTT协议下，设备通过自动注册的方式获取到设备id，key
    var httpport: String = "", //设备注册HTTP端口（MQTT参数）
    var projid: String = "", //产品ID(MQTT参数)
    var regcode: String = "", //厂商设备注册码（MQTT参数）

    /**
     * SL651水文协议特有配置参数
     */
    var type_code: String = "",//测站编码
    var co_address: String = "", //中心站地址
    var password: String = "",//密码
    var taddress: String = "", //遥测站地址
    var hour_report: String = "", //小时报开启标识  1:开启 0:关闭
    var data_link: String = "", //数据链路维持报  0|[10,40]   0:关闭
    var valid_day: String = "", //补发数据有效天数
    var reissue_time: String = "", //数据补发间隔
)