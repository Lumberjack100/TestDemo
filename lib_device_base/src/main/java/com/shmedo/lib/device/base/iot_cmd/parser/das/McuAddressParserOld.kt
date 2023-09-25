package com.shmedo.lib.device.base.iot_cmd.parser.das

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.das.McuAddressInfo

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/4/7 <br/>
 * 描述：      解析水利遥测设备地址信息
 */
 class McuAddressParserOld :
    OldIOTResultParser<McuAddressInfo?> {
    override fun parse(result: String): McuAddressInfo? {
        val info = McuAddressInfo()
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
            info.mcuaddr = keyValueMap.getOrDefault("mcuaddr", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {

    }

    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_MCU_ADDRESS
    }
}