package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataCenterStatus
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/1/9
 * 描述： TODO
 */
@IOTParser
class DataCenterStatusParser: IOTCommandParser<DataCenterStatus> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DataCenterStatus {
        return DataCenterStatus().apply {
            centerid = keyValueMap.getOrDefault("centerid", centerid.toString()).toInt()
            status = keyValueMap.getOrDefault("status", status)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_DATA_CENTER_STATUS

}