package com.shmedo.lib.device.base.md_cmd.model.common


/**
 * 创建者：gonghe
 * 创建时间：2024/4/12
 * 描述： 设备网络状态
 *
 * $$044n,(1),(2),(3),(4),(5),(6),(7),(8),(9)\r\n
 * 注：n取值1，2，3
 * （1）已发送数据
 * （2）已生成数据
 * （3）flash使能，取值0,1，1表示使能，0表示未使能
 * （4）flash读指针
 * （5）flash写指针
 * （6）中心使能：取值0,1，1表示使能，0表示未使能
 * （7）中心状态：取值0,1，1表示上线，0表示未下线
 * （8）4G模块状态：
 * （9）MQTT状态：
 *  (10)在线率，单位%
 * 示例：
 * $$0441,7,7,1,0x001000D4,0x001000D4,1,1,4,7
 */
data class DeviceNetStatus(
    var linkNumber: String = "",
    var sentData: String = "",
    var generatedData: String = "",
    var flashEnable: String = "",
    var flashReadPointer: String = "",
    var flashWritePointer: String = "",
    var linkEnable: String = "",
    var linkStatus: String = "",
    var fourGModuleStatus: String = "",
    var mqttStatus: String = "",
    var onlineRate: String = "",
)