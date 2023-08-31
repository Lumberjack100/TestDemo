package com.shmedo.lib.device.base.iot_cmd.parser.e40

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.e40.E40CORSInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2/26/21 <br></br>
 * 描述：     解析 CORS 服务参数
 */
class E40CORSInfoParser : IOTResultParser<E40CORSInfo?> {
    override fun parse(result: String): E40CORSInfo? {
        val info = E40CORSInfo()
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
            info.addr = keyValueMap.getOrDefault("addr", "NullKey")
            info.port = keyValueMap.getOrDefault("port", "NullKey")
            info.user = keyValueMap.getOrDefault("user", "NullKey")
            info.pswd = keyValueMap.getOrDefault("pswd", "NullKey")
            info.sta = keyValueMap.getOrDefault("sta", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.E40_MD_GET_CORS
    }
}