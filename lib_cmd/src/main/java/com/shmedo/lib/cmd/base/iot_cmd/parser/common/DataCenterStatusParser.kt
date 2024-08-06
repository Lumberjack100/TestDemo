package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataCenterStatus

/**
 * 创建者：gonghe
 * 创建时间：2024/1/9
 * 描述： TODO
 */
class DataCenterStatusParser: IOTCommandParser<DataCenterStatus> {

    override fun parseInstance(keyValueMap: Map<String, String>): DataCenterStatus {
        return DataCenterStatus().apply {
            centerid = keyValueMap.getOrDefault("centerid", centerid.toString()).toInt()
            status = keyValueMap.getOrDefault("status", status)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_DATA_CENTER_STATUS

}