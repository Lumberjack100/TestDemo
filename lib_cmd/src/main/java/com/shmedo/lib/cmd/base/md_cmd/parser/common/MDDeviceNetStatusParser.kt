package com.shmedo.lib.cmd.base.md_cmd.parser.common

import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.model.common.DeviceNetStatus

/**
 * 创建者：gonghe
 * 创建时间：2024/4/12
 * 描述： TODO
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
 * $$0441,0,0,1,0x00100000,0x00100000,0,0,2,3,0.0
 * $$0442,0,0,1,0x00600000,0x00600000,0,0,2,3,0.0
 * $$0443,38442,38447,1,0x00D0B01C,0x00D0B01C,1,1,4,7,100.0
 */
class MDDeviceNetStatusParser : MDCommandParser<DeviceNetStatus> {
    override fun parseInstance(values: List<String>): DeviceNetStatus {
        return DeviceNetStatus().apply {
            linkNumber = values.getOrNull(0)?.substring(5) ?: linkNumber
            sentData = values.getOrNull(1) ?: sentData
            generatedData = values.getOrNull(2) ?: generatedData
            flashEnable = values.getOrNull(3) ?: flashEnable
            flashReadPointer = values.getOrNull(4) ?: flashReadPointer
            flashWritePointer = values.getOrNull(5) ?: flashWritePointer
            linkEnable = values.getOrNull(6) ?: linkEnable
            linkStatus = values.getOrNull(7) ?: linkStatus
            fourGModuleStatus = values.getOrNull(8) ?: fourGModuleStatus
            mqttStatus = values.getOrNull(9) ?: mqttStatus
            onlineRate = values.getOrNull(10) ?: onlineRate
        }
    }

    override fun commandType(): MDCommandType = MDCommandType.QUERY_NETWORK_STATUS
}