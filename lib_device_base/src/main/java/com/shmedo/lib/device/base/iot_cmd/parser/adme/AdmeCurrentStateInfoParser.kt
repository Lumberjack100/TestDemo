package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeCurrentStateInfo

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/13
 *
 * 描述： TODO
 *
 *
 */
class AdmeCurrentStateInfoParser: IOTCommandParser<AdmeCurrentStateInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeCurrentStateInfo {
        return AdmeCurrentStateInfo().apply {
            sn = keyValueMap.getOrDefault("sn", sn)
            productid = keyValueMap.getOrDefault("productid", productid)
            simid = keyValueMap.getOrDefault("simid", simid)
            imeid = keyValueMap.getOrDefault("imeid", imeid)
            firversion = keyValueMap.getOrDefault("firversion", firversion)
            ctrinputv = keyValueMap.getOrDefault("ctrinputv", ctrinputv)
            driveinputv = keyValueMap.getOrDefault("driveinputv", driveinputv)
            inctype = keyValueMap.getOrDefault("inctype", inctype)
            incnum = keyValueMap.getOrDefault("incnum", incnum)
            incvoltage = keyValueMap.getOrDefault("incvoltage", incvoltage)
            temperature = keyValueMap.getOrDefault("temperature", temperature)
            humidity = keyValueMap.getOrDefault("humidity", humidity)
            intertempe = keyValueMap.getOrDefault("intertempe", intertempe)
            signalstr = keyValueMap.getOrDefault("signalstr", signalstr)
            incloc = keyValueMap.getOrDefault("incloc", incloc)
            abndiasis = keyValueMap.getOrDefault("abndiasis", abndiasis)
            downnum = keyValueMap.getOrDefault("downnum", downnum)
            runmileage = keyValueMap.getOrDefault("runmileage", runmileage)
            nexttime = keyValueMap.getOrDefault("nexttime", nexttime)
            testway = keyValueMap.getOrDefault("testway", testway)
            scsq = keyValueMap.getOrDefault("scsq", scsq)
            bcsq = keyValueMap.getOrDefault("bcsq", bcsq)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_EQUIPMENT_STATE
}