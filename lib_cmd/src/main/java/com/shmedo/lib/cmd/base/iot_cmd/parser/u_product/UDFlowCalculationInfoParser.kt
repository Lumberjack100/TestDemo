package com.shmedo.lib.cmd.base.iot_cmd.parser.u_product

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDFlowCalculationInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述： 一体式泥位计流量计算配置解析器
 */
@IOTParser
class UDFlowCalculationInfoParser : IOTCommandParser<UDFlowCalculationInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): UDFlowCalculationInfo {
        return UDFlowCalculationInfo().apply {
            shape = keyValueMap.getOrDefault("shape", shape)
            cannalwide = keyValueMap.getOrDefault("cannalwide", cannalwide)
            initdepth = keyValueMap.getOrDefault("initdepth", initdepth)
            initheight = keyValueMap.getOrDefault("initheight", initheight)
            maxdepth = keyValueMap.getOrDefault("maxdepth", maxdepth)
            bottomwide = keyValueMap.getOrDefault("bottomwide", bottomwide)
            sloperatio = keyValueMap.getOrDefault("sloperatio", sloperatio)
            diameter = keyValueMap.getOrDefault("diameter", diameter)
            customize = keyValueMap.getOrDefault("customize", customize)
            hcorvalue = keyValueMap.getOrDefault("hcorvalue", hcorvalue)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_MUD_LEVEL_METER_SENSOR
}
