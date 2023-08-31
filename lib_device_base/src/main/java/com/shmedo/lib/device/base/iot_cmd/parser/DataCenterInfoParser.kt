package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.DataCenterInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/18/20 <br></br>
 * 描述：   解析数据中心参数
 */
class DataCenterInfoParser : IOTResultParser<DataCenterInfo?> {
    override fun parse(result: String): DataCenterInfo? {
        val info = DataCenterInfo()
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
            info.centerid = keyValueMap.getOrDefault("centerid", "")
            info.protocol = keyValueMap.getOrDefault("protocol", "")
            info.datatype = keyValueMap.getOrDefault("datatype", "")
            info.plattype = keyValueMap.getOrDefault("plattype", "")
            info.addr = keyValueMap.getOrDefault("addr", "")
            info.port = keyValueMap.getOrDefault("port", "")
            info.deviceid = keyValueMap.getOrDefault("deviceid", "")
            info.devicekey = keyValueMap.getOrDefault("devicekey", "")
            info.httpaddr = keyValueMap.getOrDefault("httpaddr", "")
            info.httpport = keyValueMap.getOrDefault("httpport", "")
            info.projid = keyValueMap.getOrDefault("projid", "")
            info.regcode = keyValueMap.getOrDefault("regcode", "")
            info.type_code = keyValueMap.getOrDefault("type_code", "")
            info.co_address = keyValueMap.getOrDefault("co_address", "")
            info.password = keyValueMap.getOrDefault("password", "")
            info.taddress = keyValueMap.getOrDefault("taddress", "")
            info.hour_report = keyValueMap.getOrDefault("hour_report", "")
            info.data_link = keyValueMap.getOrDefault("data_link", "")
            info.valid_day = keyValueMap.getOrDefault("valid_day", "")
            info.reissue_time = keyValueMap.getOrDefault("reissue_time", "")

            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.MD_GET_DATA_CENTER
    }
}