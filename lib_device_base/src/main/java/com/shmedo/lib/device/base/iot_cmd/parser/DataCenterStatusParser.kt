package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.DataCenterStatus

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/29/20 <br></br>
 * 描述：      解析数据中心状态
 */
class DataCenterStatusParser : IOTResultParser<DataCenterStatus?> {
    override fun parse(result: String): DataCenterStatus? {
        val info = DataCenterStatus()
        return try {
            val keyValues = result.split("&").toTypedArray()
            val keyValueMap = HashMap<String, String>()
            for (keyValue in keyValues) {
                val strs = keyValue.split("=").toTypedArray()
                if (strs.size < 2) {
                    keyValueMap[strs[0]] = ""
                } else {
                    keyValueMap[strs[0]] = strs[1]
                }
            }
            info.centerid = keyValueMap["centerid"]?.toInt() ?: 0
            info.status = keyValueMap.getOrDefault("status", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.MD_GET_DATA_CENTER_STATUS
    }
}