package com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M20BaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

@IOTParser
class M20BaseInfoParser : IOTCommandParser<M20BaseInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): M20BaseInfo {
        return M20BaseInfo().apply {
            sn = keyValueMap.getOrDefault("sn", sn)
            productid = keyValueMap.getOrDefault("productid", productid)
            firversion = keyValueMap.getOrDefault("firversion", firversion)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.M20_MD_GET_BASE_INFO
}