package com.shmedo.lib.device.base.iot_cmd.parser.hac

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacHoleAreaDepthInfo
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMeasuringHoleDepthInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
class HacMeasuringHoleDepthInfoParser : IOTCommandParser<HacMeasuringHoleDepthInfo> {

    override fun parseInstance(keyValueMap: Map<String, String>): HacMeasuringHoleDepthInfo {
        return HacMeasuringHoleDepthInfo().apply {
            address = keyValueMap.getOrDefault("address", "")
            lowtbtss = keyValueMap.getOrDefault("lowtbtss", "")
            holelist = keyValueMap.getOrDefault("holelist", "").let {
                MoshiUtil.fromJson<List<HacHoleAreaDepthInfo>>(it) ?: emptyList()
            }
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PARAM
}