package com.shmedo.lib.device.base.iot_cmd.parser.vms

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.vms.VmsTerminalSensorInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/23/20 <br></br>
 * 描述：     解析Vms终端传感器数据
 */
class VmsTerminalSensorInfoParser : IOTResultParser<VmsTerminalSensorInfo?> {
    override fun parse(result: String): VmsTerminalSensorInfo? {
        val info = VmsTerminalSensorInfo()
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
            info.channel = keyValueMap.getOrDefault("channel", "NullKey")
            info.insert = keyValueMap.getOrDefault("insert", "NullKey")
            info.freqtype = keyValueMap.getOrDefault("freqtype", "NullKey")
            info.freqmax = keyValueMap.getOrDefault("freqmax", "NullKey")
            info.freqmin = keyValueMap.getOrDefault("freqmin", "NullKey")
            info.volttype = keyValueMap.getOrDefault("volttype", "NullKey")
            info.expvolt = keyValueMap.getOrDefault("expvolt", "NullKey")
            info.type = keyValueMap.getOrDefault("type", "NullKey")
            info.name = keyValueMap.getOrDefault("name", "NullKey")
            info.gateval = keyValueMap.getOrDefault("gateval", "NullKey")
            info.corral = keyValueMap.getOrDefault("corral", "NullKey")
            info.fixsite = keyValueMap.getOrDefault("fixsite", "NullKey")
            info.ropelen = keyValueMap.getOrDefault("ropelen", "NullKey")
            info.parama = keyValueMap.getOrDefault("parama", "NullKey")
            info.paramb = keyValueMap.getOrDefault("paramb", "NullKey")
            info.paramc = keyValueMap.getOrDefault("paramc", "NullKey")
            info.paramk = keyValueMap.getOrDefault("paramk", "NullKey")
            info.paramm = keyValueMap.getOrDefault("paramm", "NullKey")
            info.paramf = keyValueMap.getOrDefault("paramf", "NullKey")
            info.paramt = keyValueMap.getOrDefault("paramt", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.VMS_MD_GET_TERMINAL_CHL
    }
}