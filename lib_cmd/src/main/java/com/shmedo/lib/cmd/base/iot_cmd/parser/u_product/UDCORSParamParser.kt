package com.shmedo.lib.cmd.base.iot_cmd.parser.u_product

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCORSParam

/**
 * 创建者：gonghe
 * 创建时间：2024/8/28
 * 描述： TODO
 */
class UDCORSParamParser : IOTCommandParser<UDCORSParam> {
    override fun parseInstance(keyValueMap: Map<String, String>): UDCORSParam {
        return UDCORSParam().apply {
            use = keyValueMap.getOrDefault("use", use)
            host = keyValueMap.getOrDefault("host", host)
            port = keyValueMap.getOrDefault("port", port)
            username = keyValueMap.getOrDefault("username", username)
            password = keyValueMap.getOrDefault("password", password)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_RTK
}