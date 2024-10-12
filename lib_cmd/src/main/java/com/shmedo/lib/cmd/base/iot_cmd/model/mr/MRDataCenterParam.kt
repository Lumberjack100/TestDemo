package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/28 <br/>
 * 描述：     数据链路配置
 */
data class MRDataCenterParam(
    var centerid: String = "",
    var switch: String = "",//开启状态 0:关闭 1:开启
    var datanet: String = "",//4G开启状态 0:关闭 1:开启
    var wirednet: String = "",//有线开启状态 0:关闭 1:开启
    var line: String = "",//通信线路选择  1：4G 2：有线
    var level: String = "",//网络协议 1:ipv4 2:ipv6
    var type: String = "", //协议类型 1:TCP 2:UDP
    var addr: String = "",//数据链路地址
    var port: String = "", //数据链路端口
    var plattype: String = "", //平台类型 DIDA:地大平台  ZYWL:成都理工平台  GHIOT:米度平台 DIDA2:地大平台2 HWPEC:河南水利 MDSW:米度水文
    var datatype: String = "", //数据协议,1：TCP-C 2:MQTT 3:SL651水文协议

    /**
     * MQTT 协议特有配置参数
     */
    var projid: String = "", //产品ID(MQTT参数)
    var deviceid: String = "", //设备id（MQTT参数）,设备id、key设置为空时，设备通过自动注册的方式获取id、key
    var devicekey: String = "",//设备key（MQTT参数）
    var regcode: String = "", //产品注册码（MQTT参数）
    var httpaddr: String = "", //设备注册HTTP地址，域名或者IP（MQTT参数）,MQTT协议下，设备通过自动注册的方式获取到设备id，key
    var httpport: String = "", //设备注册HTTP端口（MQTT参数）
    var keepalive: String = "", //心跳间隔(MQTT)     链路维持报间隔(SL651   0关闭)

    /**
     * SL651水文协议特有配置参数
     */
    var type_code: String = "", //测站分类编码
    var co_address: String = "", //中心站地址  0-256
    var password: String = "", //密码 0-65535
    var taddress: String = "", //遥测站地址   10个数字
    var timed_report: String = "", //定时报开关  0:关闭 1:开启
    var hour_report: String = "",//小时报开关  0:关闭 1:开启
    var add_report: String = "",//加报报开关  0:关闭 1:开启
    var maintain_report: String = "",//维持报开关  0:关闭 1:开启
    var valid_day: String = "", //补发数据有效天数 1-180
    var reissue_time: String = "", //数据补发间隔 (min)
)
