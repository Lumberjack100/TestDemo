package com.shmedo.lib.device.base.iot_cmd.parser.das

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasBdTerminalInfo
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/20 <br></br>
 * 描述：     解析DAS  北斗数传终端参数
 */
class DasBdTerminalInfoParserOld :
    OldIOTResultParser<DasBdTerminalInfo?> {
    override fun parse(result: String): DasBdTerminalInfo? {
        val info = DasBdTerminalInfo()
        return try {
            val keyValues = result.split("&").toTypedArray()
            val keyValueMap = HashMap<String, String>()
            for (keyValue in keyValues) {
                val strs = keyValue.split("=").toTypedArray()
                if (strs.size < 2) {
                    keyValueMap[strs[0]] = ""
                } else {
                    keyValueMap[strs[0]] = strs[1]
                }
            }
            info.sw = keyValueMap.getOrDefault("sw", "NullKey")
            info.dstaddr = keyValueMap.getOrDefault("dstaddr", "NullKey")
            info.baud = keyValueMap.getOrDefault("baud", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_BD_TERMINAL
    }
}