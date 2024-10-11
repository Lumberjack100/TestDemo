package com.shmedo.lib.cmd.base.md_cmd.parser.common

import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.model.common.BleDataCenterInfo
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/4/12
 * 描述： 解析 BleDataCenterInfo
 *
 * $$8893,1,4,mqtt.shmedo.com 6883,300,150000L,150000L,a84b42b1-cb30-410f-8285-5f4de6f9d319,
 * 2,mqtt.shmedo.com 80,fXQQROerSlJ0bqTPCoMnyqgR-2dzhytztk3eYV6nuA0OBQljkqG_exXYtNfr,,,
 * 查询数据链路参数：##889n\r\n，其中 n：表示中心编号，取值1，2，3
 * 返回参数：##889n,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12)\r\n
 * (1):数据链路开关，0：关闭，1：打开
 * (2):通讯协议，2：MDM协议，4：MQTT自动注册，5：MQTT手动注册
 * (3):数据平台地址
 * (4):keepAlive
 * (5):设备SN号
 * (6):产品ID
 * (7):注册码
 * (8):注册平台类型，0：地大平台，1：成都理工平台，2：米度平台
 * (9):注册平台地址
 * (10):APPKey
 * (11):MQTT设备ID
 * (12):MQTT用户名
 * (13):MQTT密码
 * 注：不同通讯协议下的参数不一致，不存在的参数，逗号之间为空
 */
class MDBleDataCenterInfoParser : MDCommandParser<BleDataCenterInfo> {
    override fun parseInstance(values: List<String>): BleDataCenterInfo {
        return BleDataCenterInfo().apply {
            communicationProtocol = values.getOrNull(2) ?: communicationProtocol
            values.getOrNull(3)?.let {
                val split = it.split(MDConstants.COMMAND_SPLICER_WHITESPACE)
                dataPlatformAddress = split.getOrNull(0) ?: dataPlatformAddress
                dataPlatformPort = split.getOrNull(1) ?: dataPlatformPort
            }
            keepAliveValue = values.getOrNull(4) ?: keepAliveValue
            deviceSn = values.getOrNull(5) ?: deviceSn
            productId = values.getOrNull(6) ?: productId
            registerCode = values.getOrNull(7) ?: registerCode
            registerPlatform = values.getOrNull(8) ?: registerPlatform
            values.getOrNull(9)?.let {
                val split = it.split(MDConstants.COMMAND_SPLICER_WHITESPACE)
                registerPlatformAddress = split.getOrNull(0) ?: registerPlatformAddress
                registerPlatformPort = split.getOrNull(1) ?: registerPlatformPort
            }
            appKey = values.getOrNull(10) ?: appKey
            mqttDeviceId = values.getOrNull(11) ?: mqttDeviceId
            mqttUsername = values.getOrNull(12) ?: mqttUsername
            mqttPassword = values.getOrNull(13) ?: mqttPassword
        }
    }

    override fun commandType(): MDCommandType = MDCommandType.QUERY_DATA_CENTER_PARAM
}