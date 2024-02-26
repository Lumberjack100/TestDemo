package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeInclinometerInfo
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/14
 *
 * 描述： TODO
 *
 *
 */
class AdmeInclinometerInfoParser: IOTCommandParser<AdmeInclinometerInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeInclinometerInfo {
        return AdmeInclinometerInfo().apply {
            inctype = keyValueMap.getOrDefault("inctype", inctype)
            address = keyValueMap.getOrDefault("address", address)
            collinval = keyValueMap.getOrDefault("collinval", collinval)
            calcinval = keyValueMap.getOrDefault("calcinval", calcinval)
            dormancytime = keyValueMap.getOrDefault("dormancytime", dormancytime)
            interupdate = keyValueMap.getOrDefault("interupdate", interupdate)
            mode = keyValueMap.getOrDefault("mode", mode)
            incversion = keyValueMap.getOrDefault("incversion", incversion)
            compenway = keyValueMap.getOrDefault("compenway", compenway)
            torangle = keyValueMap.getOrDefault("torangle", torangle)
            swtor_angle = keyValueMap.getOrDefault("torangle", IOTConstants.NULL_KEY)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_INCLINOMETER
}