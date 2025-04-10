package com.shmedo.lib.cmd.base.iot_cmd.parser.m50

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50CorsParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2025/4/10
 * 描述： TODO
 */
@IOTParser
class M50CorsParamParser: IOTCommandParser<M50CorsParam> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): M50CorsParam {
        return M50CorsParam().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
            host = keyValueMap.getOrDefault("host", host)
            port = keyValueMap.getOrDefault("port", port)
            username = keyValueMap.getOrDefault("username", username)
            password = keyValueMap.getOrDefault("password", password)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.M50_MD_GET_CORS
}