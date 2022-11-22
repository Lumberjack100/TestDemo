package com.shmedo.configlibrary.iot.cmd.parser.das

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.das.DasBaseInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：      解析DAS 状态页面基本信息参数
 */
class DasBaseInfoParser : IOTResultParser<DasBaseInfo?> {
    override fun parse(result: String): DasBaseInfo? {
        val info = DasBaseInfo()
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
            info.iccid = keyValueMap.getOrDefault("iccid", "NullKey")
            info.imei = keyValueMap.getOrDefault("imei", "NullKey")
            info.ver = keyValueMap.getOrDefault("ver", "NullKey")
            info.local = keyValueMap.getOrDefault("local", "NullKey")
            info.involt = keyValueMap.getOrDefault("involt", "NullKey")
            info.outvolt = keyValueMap.getOrDefault("outvolt", "NullKey")
            info.csq = keyValueMap.getOrDefault("csq", "NullKey")
            info.isp = keyValueMap.getOrDefault("isp", "NullKey")
            info.code = keyValueMap.getOrDefault("code", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_DEVICE_BASE
    }
}