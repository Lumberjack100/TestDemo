package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeBaseInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/23/20 <br></br>
 * 描述：      解析ADME基础信息
 */
class AdmeBaseInfoParser : IOTResultParser<AdmeBaseInfo?> {
    override fun parse(result: String): AdmeBaseInfo?{
        val info = AdmeBaseInfo()
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
            info.equimodel = keyValueMap.getOrDefault("equimodel", "NullKey")
            info.online = keyValueMap.getOrDefault("online", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS
    }
}