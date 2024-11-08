package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeAndNegativeTestExceptionHandlingInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/3/27
 * 描述： TODO
 */
@IOTParser
class AdmeAndNegativeTestExceptionHandlingInfoParser : IOTCommandParser<AdmeAndNegativeTestExceptionHandlingInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeAndNegativeTestExceptionHandlingInfo {
        return AdmeAndNegativeTestExceptionHandlingInfo().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING
}