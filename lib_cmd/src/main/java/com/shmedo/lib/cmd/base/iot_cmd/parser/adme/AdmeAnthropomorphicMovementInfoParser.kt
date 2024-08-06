package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeAnthropomorphicMovementInfo

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/15
 *
 * 描述： TODO
 *
 *
 */
class AdmeAnthropomorphicMovementInfoParser: IOTCommandParser<AdmeAnthropomorphicMovementInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeAnthropomorphicMovementInfo {
        return AdmeAnthropomorphicMovementInfo().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE

}