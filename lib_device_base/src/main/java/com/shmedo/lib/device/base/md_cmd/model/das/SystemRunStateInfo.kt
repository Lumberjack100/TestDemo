package com.shmedo.lib.device.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： 系统运行状态实体类
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
data class SystemRunStateInfo(
    val gprsSignal: String = "",
    val gpsNumber: String = "",
    val systemStartUp: String = "",
    val systemRestart: String = "",
    val simCCID: String = "",
    val internalTemperature: String = "",
    val batteryVoltage: String = "",
    val externalVoltage: String = "",
    val operatorType: String = "",
    val networkMode: String = ""
)
