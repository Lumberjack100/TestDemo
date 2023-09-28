package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandResponseParser
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRWirelessNet

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     TODO
 */
class MRWirelessNetParser : IOTCommandResponseParser<MRWirelessNet> {

    override fun parseInstance(keyValueMap: Map<String, String>): MRWirelessNet {
        return MRWirelessNet().apply {
            switch = keyValueMap.getOrDefault("switch", switch)
            apn = keyValueMap.getOrDefault("apn", apn)
            username = keyValueMap.getOrDefault("username", username)
            password = keyValueMap.getOrDefault("password", password)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_DATA_NETWORK

}