package com.shmedo.lib.device.base.iot_cmd.parser.m20

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandResponseParser
import com.shmedo.lib.device.base.iot_cmd.model.m20.M20BaseInfo

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */

class M20BaseInfoParser : IOTCommandResponseParser<M20BaseInfo> {

    override fun parseInstance(keyValueMap: Map<String, String>): M20BaseInfo {
        return M20BaseInfo().apply {
            sn = keyValueMap.getOrDefault("sn", sn)
            productid = keyValueMap.getOrDefault("productid", productid)
            firversion = keyValueMap.getOrDefault("firversion", firversion)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.M20_MD_GET_BASE_INFO
}


