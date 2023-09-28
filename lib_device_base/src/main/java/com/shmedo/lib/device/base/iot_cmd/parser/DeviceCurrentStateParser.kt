package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandResponseParser
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */
class DeviceCurrentStateParser: IOTCommandResponseParser<String> {
    override fun parseInstance(keyValueMap: Map<String, String>): String {
        return keyValueMap["state"]!!
    }

    override fun commandType(): IOTCommandType = IOTCommandType.QUERY_DEVICE_STATUS
}