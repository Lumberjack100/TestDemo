package com.shmedo.lib.cmd.base.md_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoOne

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 *
 * $$041,(1),(2),(3),(4),(5),(6)\r\n
 * （1）SN号
 * （2）IMEI号
 * （3）SIM卡号
 * （4）启动代码1
 * （5）启动代码2
 * （6）信号强度，1~11为1格信号，12~18为2格信号，19~25为3格信号，26~31为4格信号
 * 示例：
 * $$041,150000L,865860047575320,898604061918C0643348,20,1,9
 */
class MDDeviceStatusInfoOneParser : MDCommandParser<DeviceStatusInfoOne> {
    override fun parseInstance(values: List<String>): DeviceStatusInfoOne {
        return DeviceStatusInfoOne(
            snNumber = values.getOrNull(1) ?: "",
            imeiNumber = values.getOrNull(2) ?: IOTConstants.NULL_KEY,
            simNumber = values.getOrNull(3) ?: IOTConstants.NULL_KEY,
            startCodeOne = values.getOrNull(4) ?: IOTConstants.NULL_KEY,
            startCodeTwo = values.getOrNull(5) ?: IOTConstants.NULL_KEY,
            signalStrength = values.getOrNull(6) ?: IOTConstants.NULL_KEY
        )
    }

    override fun commandType(): MDCommandType = MDCommandType.QUERY_DAS_STATUS_1
}