package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */
class DeviceCurrentStateParser: IOTCommandParser<String> {
    override fun parseInstance(keyValueMap: Map<String, String>): String {
        return keyValueMap["state"]!!
    }

    override fun commandType(): IOTCommandType = IOTCommandType.QUERY_DEVICE_STATUS
}