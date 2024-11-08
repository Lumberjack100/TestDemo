package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeWorkModeInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/1/4
 * 描述： TODO
 */
@IOTParser
class AdmeWorkModeInfoParser: IOTCommandParser<AdmeWorkModeInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeWorkModeInfo {
        return AdmeWorkModeInfo().apply {
            workmode = keyValueMap.getOrDefault("workmode", workmode)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_WORK_MODE
}