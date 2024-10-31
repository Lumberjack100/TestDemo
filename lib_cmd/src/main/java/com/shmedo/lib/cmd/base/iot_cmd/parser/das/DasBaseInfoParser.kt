package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/2/26
 * 描述： TODO
 */
@IOTParser
class DasBaseInfoParser: IOTCommandParser<DasBaseInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DasBaseInfo {
        return DasBaseInfo().apply {
            sn = keyValueMap.getOrDefault("sn", sn)
            iccid = keyValueMap.getOrDefault("iccid", iccid)
            imei = keyValueMap.getOrDefault("imei", imei)
            ver = keyValueMap.getOrDefault("ver", ver)
            local = keyValueMap.getOrDefault("local", local)
            involt = keyValueMap.getOrDefault("involt", involt)
            outvolt = keyValueMap.getOrDefault("outvolt", outvolt)
            csq = keyValueMap.getOrDefault("csq", csq)
            isp = keyValueMap.getOrDefault("isp", isp)
            code = keyValueMap.getOrDefault("code", code)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.DAS_MD_GET_DEVICE_BASE
}