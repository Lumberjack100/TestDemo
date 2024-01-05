package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeWorkModeInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/1/4
 * 描述： TODO
 */
class AdmeWorkModeInfoParser: IOTCommandParser<AdmeWorkModeInfo> {

    override fun parseInstance(keyValueMap: Map<String, String>): AdmeWorkModeInfo {
        return AdmeWorkModeInfo().apply {
            workmode = keyValueMap.getOrDefault("workmode", workmode)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_WORK_MODE
}