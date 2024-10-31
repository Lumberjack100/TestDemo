package com.shmedo.lib.cmd.base.iot_cmd.parser.hac

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMeasuringDataInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/5/19
 * 描述： TODO
 */
@IOTParser
class HacMeasuringDataInfoParser : IOTCommandParser<HacMeasuringDataInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): HacMeasuringDataInfo {
        return HacMeasuringDataInfo().apply {
            equipmodel = keyValueMap.getOrDefault("equipmodel", equipmodel)
            address = keyValueMap.getOrDefault("address", address)
            downwaitetime = keyValueMap.getOrDefault("downwaitetime", downwaitetime)
            datatype = keyValueMap.getOrDefault("datatype", datatype)
            onewaytest = keyValueMap.getOrDefault("onewaytest", onewaytest)
            checkreverse = keyValueMap.getOrDefault("checkreverse", checkreverse)
            currhole = keyValueMap.getOrDefault("currhole", currhole)
            holelist = keyValueMap.getOrDefault("holelist", holelist)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_HAC_MD_GET_DATA_MEASURE_PARAM
}