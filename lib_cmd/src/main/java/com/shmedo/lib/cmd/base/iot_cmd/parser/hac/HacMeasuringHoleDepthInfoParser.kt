package com.shmedo.lib.cmd.base.iot_cmd.parser.hac

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacHoleAreaDepthInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMeasuringHoleDepthInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
@IOTParser
class HacMeasuringHoleDepthInfoParser : IOTCommandParser<HacMeasuringHoleDepthInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): HacMeasuringHoleDepthInfo {
        return HacMeasuringHoleDepthInfo().apply {
            address = keyValueMap.getOrDefault("address", "")
            lowtbtss = keyValueMap.getOrDefault("lowtbtss", "")
            holelist = keyValueMap.getOrDefault("holelist", "").let {
                MoshiUtil.fromJson<List<HacHoleAreaDepthInfo>>(it) ?: emptyList()
            }
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PARAM
}