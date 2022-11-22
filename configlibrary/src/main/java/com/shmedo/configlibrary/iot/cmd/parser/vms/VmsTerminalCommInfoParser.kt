package com.shmedo.configlibrary.iot.cmd.parser.vms

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalCommInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/2/20 <br></br>
 * 描述：    解析Vms终端通信参数
 */
class VmsTerminalCommInfoParser : IOTResultParser<VmsTerminalCommInfo?> {
    override fun parse(result: String): VmsTerminalCommInfo? {
        val info = VmsTerminalCommInfo()
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
            info.netid = keyValueMap.getOrDefault("netid", "NullKey")
            info.dstaddr = keyValueMap.getOrDefault("dstaddr", "NullKey")
            info.channel = keyValueMap.getOrDefault("channel", "NullKey")
            info.airbaud = keyValueMap.getOrDefault("airbaud", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.VMS_MD_GET_TERMINAL_COMMUNICATE
    }
}