package com.shmedo.configlibrary.iot.cmd.parser.vms

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.vms.VmsAisleInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/15/20 <br></br>
 * 描述：    解析Vms网关通道参数
 */
class VmsAisleInfoParser : IOTResultParser<VmsAisleInfo?> {
    override fun parse(result: String): VmsAisleInfo? {
        val info = VmsAisleInfo()
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
            info.channel = keyValueMap["channel"]?.toInt() ?: 0
            info.netid = keyValueMap.getOrDefault("netid", "NullKey")
            info.addr = keyValueMap.getOrDefault("addr", "NullKey")
            info.chl = keyValueMap.getOrDefault("chl", "NullKey")
            info.airbaud = keyValueMap.getOrDefault("airbaud", "NullKey")
            info.ppt = keyValueMap.getOrDefault("ppt", "NullKey")
            info.terminalmode = keyValueMap.getOrDefault("terminalmode", "NullKey")
            info.sendgap = keyValueMap.getOrDefault("sendgap", "NullKey")
            info.offline = keyValueMap.getOrDefault("offline", "NullKey")
            info.sleepgap = keyValueMap.getOrDefault("sleepgap", "NullKey")
            info.wakeupgap = keyValueMap.getOrDefault("wakeupgap", "NullKey")
            info.terminalnum = keyValueMap.getOrDefault("terminalnum", "NullKey")
            info.rssi = keyValueMap.getOrDefault("rssi", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.VMS_MD_GET_GATEWAY_PARAM
    }
}