package com.shmedo.lib.device.base.iot_cmd.parser.rn20

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.rn20.Rn20BaseInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/8/3 <br></br>
 * 描述：     解析雨量采集器基本信息
 */
class Rn20BaseInfoParser : IOTResultParser<Rn20BaseInfo?> {
    override fun parse(result: String): Rn20BaseInfo? {
        val info = Rn20BaseInfo()
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
            info.ver = keyValueMap.getOrDefault("ver", "NullKey")
            info.local = keyValueMap.getOrDefault("local", "NullKey")
            info.involt = keyValueMap.getOrDefault("involt", "NullKey")
            info.ssi = keyValueMap.getOrDefault("ssi", "NullKey")
            info.recvbuf = keyValueMap.getOrDefault("recvbuf", "NullKey")
            info.sendbuf = keyValueMap.getOrDefault("sendbuf", "NullKey")
            info.netid = keyValueMap.getOrDefault("netid", "NullKey")
            info.addr = keyValueMap.getOrDefault("addr", "NullKey")
            info.channel = keyValueMap.getOrDefault("channel", "NullKey")
            info.finaltime = keyValueMap.getOrDefault("finaltime", "NullKey")
            info.logintime = keyValueMap.getOrDefault("logintime", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.RN20_MD_GET_TERMINAL_BASE
    }
}