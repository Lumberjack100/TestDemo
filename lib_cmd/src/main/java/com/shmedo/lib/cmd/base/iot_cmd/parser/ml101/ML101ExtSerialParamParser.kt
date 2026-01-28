package com.shmedo.lib.cmd.base.iot_cmd.parser.ml101

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.ml101.ML101ExtSerialParamInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：ML101 扩展串口参数信息解析器
 *
 * 解析 MD_CFG_EXT_SERIAL_PARAM (md_cfgextserialparam) 指令的响应数据
 *
 * 指令响应格式：
 * ```
 * $cmd=md_cfgextserialparam&method=0&rs485_sw=1&rs485_mode=2&rs485_baud=9600
 * &rs485_paritybit=N&rs485_dataBit=8&rs485_stopbit=1&rs232_sw=1&rs232_mode=2
 * &rs232_baud=115200&rs232_paritybit=N&rs232_dataBit=8&rs232_stopbit=1
 * ```
 *
 * 参数说明：
 * - method: 功能码 (0-查询, 1-设置)
 * - rs485_sw: RS485 开关 (0-开, 1-关)
 * - rs485_mode: RS485 功能模式 (0-关闭输出, 1-日志输出, 2-数据透传)
 * - rs485_baud: RS485 波特率
 * - rs485_paritybit: RS485 校验位
 * - rs485_dataBit: RS485 数据位
 * - rs485_stopbit: RS485 停止位
 * - rs232_*: RS232 相关参数
 */
@IOTParser
class ML101ExtSerialParamParser : IOTCommandParser<ML101ExtSerialParamInfo> {

    /**
     * 解析键值对为 ML101ExtSerialParamInfo 对象
     *
     * @param keyValueMap 从指令响应中解析的键值对 Map
     * @return ML101ExtSerialParamInfo 扩展串口参数信息对象
     */
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): ML101ExtSerialParamInfo {
        return ML101ExtSerialParamInfo(
            // RS485 参数
            rs485_sw = keyValueMap["rs485_sw"]?.toIntOrNull() ?: 0,
            rs485_mode = keyValueMap["rs485_mode"]?.toIntOrNull() ?: 2,
            rs485_baud = keyValueMap["rs485_baud"]?.toIntOrNull() ?: 115200,
            rs485_paritybit = keyValueMap.getOrDefault("rs485_paritybit", "N"),
            rs485_dataBit = keyValueMap["rs485_dataBit"]?.toIntOrNull() ?: 8,
            rs485_stopbit = keyValueMap.getOrDefault("rs485_stopbit", "1"),
            // RS232 参数
            rs232_sw = keyValueMap["rs232_sw"]?.toIntOrNull() ?: 0,
            rs232_mode = keyValueMap["rs232_mode"]?.toIntOrNull() ?: 2,
            rs232_baud = keyValueMap["rs232_baud"]?.toIntOrNull() ?: 115200,
            rs232_paritybit = keyValueMap.getOrDefault("rs232_paritybit", "N"),
            rs232_dataBit = keyValueMap["rs232_dataBit"]?.toIntOrNull() ?: 8,
            rs232_stopbit = keyValueMap.getOrDefault("rs232_stopbit", "1")
        )
    }

    /**
     * 指令类型：查询/设置扩展串口参数
     */
    override val commandType: IOTCommandType = IOTCommandType.MD_CFG_EXT_SERIAL_PARAM
}
