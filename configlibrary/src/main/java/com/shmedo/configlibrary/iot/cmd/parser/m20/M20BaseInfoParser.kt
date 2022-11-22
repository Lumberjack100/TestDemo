package com.shmedo.configlibrary.iot.cmd.parser.m20

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.m20.M20BaseInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/27/21 <br></br>
 * 描述：    解析M20基本信息
 */
class M20BaseInfoParser : IOTResultParser<M20BaseInfo?> {
    override fun parse(result: String): M20BaseInfo? {
        val info = M20BaseInfo()
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
            info.productid = keyValueMap.getOrDefault("productid", "NullKey")
            info.firversion = keyValueMap.getOrDefault("firversion", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.M20_MD_GET_BASE_INFO
    }
}