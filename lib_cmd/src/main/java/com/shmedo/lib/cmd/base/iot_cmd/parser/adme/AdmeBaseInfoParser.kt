package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/11
 *
 * 描述： TODO
 *
 *
 */
@IOTParser
class AdmeBaseInfoParser: IOTCommandParser<AdmeBaseInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeBaseInfo {
        return AdmeBaseInfo().apply {
            sn = keyValueMap.getOrDefault("sn", sn)
            productid = keyValueMap.getOrDefault("productid", productid)
            equimodel = keyValueMap.getOrDefault("equimodel", equimodel)
            online = keyValueMap.getOrDefault("online", online)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS
}