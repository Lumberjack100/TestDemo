package com.shmedo.lib.cmd.base.iot_cmd.parser.m50

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.m50.M50NetworkConfigParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2024/8/25 <br/>
 * 描述：     M50网络配置解析类
 */
@IOTParser
class M50NetworkConfigParser : IOTCommandParser<M50NetworkConfigParam> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): M50NetworkConfigParam {
        return M50NetworkConfigParam().apply {
            switch = keyValueMap.getOrDefault("switch", switch)
            networkType = keyValueMap.getOrDefault("networkType", networkType)
            apn = keyValueMap.getOrDefault("apn", apn)
            username = keyValueMap.getOrDefault("username", username)
            password = keyValueMap.getOrDefault("password", password)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_MR_GET_DATA_NETWORK
} 