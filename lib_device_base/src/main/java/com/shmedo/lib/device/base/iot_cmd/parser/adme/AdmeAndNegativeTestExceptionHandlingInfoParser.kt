package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeAndNegativeTestExceptionHandlingInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/3/27
 * 描述： TODO
 */
class AdmeAndNegativeTestExceptionHandlingInfoParser : IOTCommandParser<AdmeAndNegativeTestExceptionHandlingInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeAndNegativeTestExceptionHandlingInfo {
        return AdmeAndNegativeTestExceptionHandlingInfo().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING
}