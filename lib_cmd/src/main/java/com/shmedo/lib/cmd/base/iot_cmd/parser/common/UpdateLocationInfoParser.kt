package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser

/**
 * 创建者：gonghe
 * 创建时间：2024/1/9
 * 描述： TODO
 */
class UpdateLocationInfoParser: IOTCommandParser<Map<String, String>> {

    override fun parseInstance(keyValueMap: Map<String, String>): Map<String, String> {
        return keyValueMap
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_INSTALL_LOCATION

}