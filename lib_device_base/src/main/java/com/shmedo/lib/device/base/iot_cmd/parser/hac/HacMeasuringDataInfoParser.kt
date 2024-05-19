package com.shmedo.lib.device.base.iot_cmd.parser.hac

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMeasuringDataInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/5/19
 * 描述： TODO
 */
class HacMeasuringDataInfoParser : IOTCommandParser<HacMeasuringDataInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): HacMeasuringDataInfo {
        return HacMeasuringDataInfo().apply {
            equipmodel = keyValueMap.getOrDefault("equipmodel", equipmodel)
            address = keyValueMap.getOrDefault("address", address)
            downwaitetime = keyValueMap.getOrDefault("downwaitetime", downwaitetime)
            datatype = keyValueMap.getOrDefault("datatype", datatype)
            onewaytest = keyValueMap.getOrDefault("onewaytest", onewaytest)
            checkreverse = keyValueMap.getOrDefault("checkreverse", checkreverse)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_HAC_MD_GET_DATA_MEASURE_PARAM
}