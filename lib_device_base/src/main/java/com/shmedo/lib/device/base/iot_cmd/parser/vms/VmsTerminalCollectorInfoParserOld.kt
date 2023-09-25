package com.shmedo.lib.device.base.iot_cmd.parser.vms

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.vms.VmsTerminalCollectorInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/2/20 <br></br>
 * 描述：    解析Vms终端采集参数
 */
class VmsTerminalCollectorInfoParserOld :
    OldIOTResultParser<VmsTerminalCollectorInfo?> {
    override fun parse(result: String): VmsTerminalCollectorInfo? {
        val info = VmsTerminalCollectorInfo()
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
            info.reptgap = keyValueMap.getOrDefault("reptgap", "NullKey")
            info.repttype = keyValueMap.getOrDefault("repttype", "NullKey")
            info.filtertype = keyValueMap.getOrDefault("filtertype", "NullKey")
            info.filternum = keyValueMap.getOrDefault("filternum", "NullKey")
            info.collgap = keyValueMap.getOrDefault("collgap", "NullKey")
            info.waitgap = keyValueMap.getOrDefault("waitgap", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.VMS_MD_GET_TERMINAL_COLLECTOR
    }
}