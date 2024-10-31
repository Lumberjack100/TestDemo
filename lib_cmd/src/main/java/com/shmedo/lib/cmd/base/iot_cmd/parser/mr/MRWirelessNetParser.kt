package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRWirelessNet
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     TODO
 */
@IOTParser
class MRWirelessNetParser : IOTCommandParser<MRWirelessNet> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRWirelessNet {
        return MRWirelessNet().apply {
            switch = keyValueMap.getOrDefault("switch", switch)
            apn = keyValueMap.getOrDefault("apn", apn)
            username = keyValueMap.getOrDefault("username", username)
            password = keyValueMap.getOrDefault("password", password)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_MR_GET_DATA_NETWORK

}