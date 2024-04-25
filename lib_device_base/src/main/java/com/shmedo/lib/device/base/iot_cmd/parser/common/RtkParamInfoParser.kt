package com.shmedo.lib.device.base.iot_cmd.parser.common

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.common.RtkParamInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/25
 * 描述： TODO
 */
class RtkParamInfoParser: IOTCommandParser<RtkParamInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): RtkParamInfo {
        return RtkParamInfo().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
            sw = keyValueMap.getOrDefault("sw", sw)
            frontCalc = keyValueMap.getOrDefault("frontCalc", frontCalc)
            baseStationMode = keyValueMap.getOrDefault("baseStationMode", baseStationMode)
            latitude = keyValueMap.getOrDefault("latitude", latitude)
            longitude = keyValueMap.getOrDefault("longitude", longitude)
            height = keyValueMap.getOrDefault("height", height)
            distance = keyValueMap.getOrDefault("distance", distance)
            time = keyValueMap.getOrDefault("time", time)
            id = keyValueMap.getOrDefault("id", id)
            gateAngleVal1 = keyValueMap.getOrDefault("gateAngleVal1", gateAngleVal1)
            gateAngleVal2 = keyValueMap.getOrDefault("gateAngleVal2", gateAngleVal2)
            gateAngleVal3 = keyValueMap.getOrDefault("gateAngleVal3", gateAngleVal3)
            gateAngleVal4 = keyValueMap.getOrDefault("gateAngleVal4", gateAngleVal4)
            gateDevVal1 = keyValueMap.getOrDefault("gateDevVal1", gateDevVal1)
            gateDevVal2 = keyValueMap.getOrDefault("gateDevVal2", gateDevVal2)
            gateDevVal3 = keyValueMap.getOrDefault("gateDevVal3", gateDevVal3)
            gateDevVal4 = keyValueMap.getOrDefault("gateDevVal4", gateDevVal4)
            rtkMode = keyValueMap.getOrDefault("rtkMode", rtkMode)
            obs = keyValueMap.getOrDefault("obs", obs)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_CFG_RTK
}