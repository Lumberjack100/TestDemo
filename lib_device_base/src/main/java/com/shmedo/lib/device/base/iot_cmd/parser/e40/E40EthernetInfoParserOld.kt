package com.shmedo.lib.device.base.iot_cmd.parser.e40

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.e40.E40EthernetInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/1/21 <br></br>
 * 描述：     解析 有线网络参数
 */
class E40EthernetInfoParserOld :
    OldIOTResultParser<E40EthernetInfo?> {
    override fun parse(result: String): E40EthernetInfo? {
        val info = E40EthernetInfo()
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
            info.dhcp = keyValueMap.getOrDefault("dhcp", "NullKey")
            info.ip = keyValueMap.getOrDefault("ip", "NullKey")
            info.netmask = keyValueMap.getOrDefault("netmask", "NullKey")
            info.gateway = keyValueMap.getOrDefault("gateway", "NullKey")
            info.dns = keyValueMap.getOrDefault("dns", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.E40_MD_GET_ETHERNET
    }
}