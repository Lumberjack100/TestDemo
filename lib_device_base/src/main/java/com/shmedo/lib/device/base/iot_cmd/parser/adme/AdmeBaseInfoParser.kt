package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeBaseInfo

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/11
 *
 * 描述： TODO
 *
 *
 */
class AdmeBaseInfoParser: IOTCommandParser<AdmeBaseInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeBaseInfo {
        return AdmeBaseInfo().apply {
            sn = keyValueMap.getOrDefault("sn", sn)
            productid = keyValueMap.getOrDefault("productid", productid)
            equimodel = keyValueMap.getOrDefault("equimodel", equimodel)
            online = keyValueMap.getOrDefault("online", online)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS
}