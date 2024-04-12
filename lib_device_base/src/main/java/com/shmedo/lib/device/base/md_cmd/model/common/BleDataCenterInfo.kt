package com.shmedo.lib.device.base.md_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/12
 * 描述： TODO
 *
 * $$8893,1,4,mqtt.shmedo.com 6883,300,150000L,150000L,a84b42b1-cb30-410f-8285-5f4de6f9d319,
 * 2,mqtt.shmedo.com 80,fXQQROerSlJ0bqTPCoMnyqgR-2dzhytztk3eYV6nuA0OBQljkqG_exXYtNfr,,,
 * 查询数据中心参数：##889n\r\n，其中 n：表示中心编号，取值1，2，3
 * 返回参数：##889n,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12)\r\n
 * (1):数据中心开关，0：关闭，1：打开
 * (2):通讯协议，2：MDM协议，4：MQTT自动注册，5：MQTT手动注册
 * (3):数据平台地址
 * (4):keepAlive
 * (5):设备SN号
 * (6):产品ID
 * (7):注册码
 * (8):注册平台类型，0：地大平台，1：成都理工平台，2：米度平台
 * (9):注册平台地址
 * (9):APPKey
 * (10):MQTT设备ID
 * (11):MQTT用户名
 * (12):MQTT密码
 * 注：不同通讯协议下的参数不一致，不存在的参数，逗号之间为空
 */
data class BleDataCenterInfo(
    var communicationProtocol: String = "",
    var dataPlatformAddress: String = "",//数据平台地址
    var dataPlatformPort: String = "",//数据平台端口号
    var keepAliveValue: String = "",
    var deviceSn: String = "",//设备SN号
    var productId: String = "",//产品ID
    var registerCode: String = "",//注册码
    var registerPlatform: String = "",//注册平台类型
    var registerPlatformAddress: String = "",//注册平台地址
    var registerPlatformPort: String = "",//注册平台端口号
    var appKey: String = "",
    var mqttDeviceId: String = "",//MQTT设备ID
    var mqttUsername: String = "",//MQTT用户名
    var mqttPassword: String = ""//MQTT密码
)
