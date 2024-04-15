package com.shmedo.lib.device.base.md_cmd.parser.das

import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.model.das.SystemRunStateInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 *
 * 获取信号强度 ##014\r\n<br/>
 * 应答:$$014,(1),(2) ,(3),(4) ,(5),(6) ,(7),(8), (9),(10) \r\n<br/>
 * (1)：信号值<br/>
 * (2)：GPS定位搜星数目<br/>
 * (3)：启动代码<br/>
 * (4)：重启代码<br/>
 * (5)：sim卡ccid<br/>
 * (6)：设备内部温度<br/>
 * (7)：设备内部电池电压<br/>
 * (8)：设备外部电压<br/>
 * (9)：运营商类型<br/>
 * (10)：网络制式<br/>
 */
class MDSystemRunStateInfoParser: MDCommandParser<SystemRunStateInfo> {

    override fun parseInstance(values: List<String>): SystemRunStateInfo {
        return SystemRunStateInfo(
            gprsSignal = values.getOrNull(1) ?: "",
            gpsNumber = values.getOrNull(2) ?: "",
            systemStartUp = values.getOrNull(3) ?: "",
            systemRestart = values.getOrNull(4) ?: "",
            simCCID = values.getOrNull(5) ?: "",
            internalTemperature = values.getOrNull(6) ?: "",
            batteryVoltage = values.getOrNull(7) ?: "",
            externalVoltage = values.getOrNull(8) ?: "",
            operatorType = values.getOrNull(9) ?: "",
            networkMode = values.getOrNull(10) ?: ""
        )
    }

    override fun commandType(): MDCommandType = MDCommandType.SYSTEM_RUN_STATE
}