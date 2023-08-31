package com.shmedo.lib.device.base.iot_cmd.parser.vms

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.vms.VmsBasicInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/11/12 <br></br>
 * 描述：   解析Vms网关基础信息
 */
class VmsBasicInfoParser : IOTResultParser<VmsBasicInfo?> {
    override fun parse(result: String): VmsBasicInfo? {
        val info = VmsBasicInfo()
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
            info.sn = keyValueMap.getOrDefault("sn", "NullKey")
            info.online = keyValueMap.getOrDefault("online", "NullKey")
            info.swVersion = keyValueMap.getOrDefault("sw", "NullKey")
            info.volt = keyValueMap.getOrDefault("volt", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.VMS_MD_GET_GATEWAY_BASE
    }
}