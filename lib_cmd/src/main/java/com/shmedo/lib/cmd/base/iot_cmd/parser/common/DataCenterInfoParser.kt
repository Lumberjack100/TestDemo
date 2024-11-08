package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataCenterInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/1/9
 * 描述： TODO
 */
@IOTParser
class DataCenterInfoParser: IOTCommandParser<DataCenterInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DataCenterInfo {
        return DataCenterInfo().apply {
            centerid = keyValueMap.getOrDefault("centerid", centerid.toString())
            protocol = keyValueMap.getOrDefault("protocol", protocol)
            datatype = keyValueMap.getOrDefault("datatype", datatype)
            plattype = keyValueMap.getOrDefault("plattype", plattype)
            addr = keyValueMap.getOrDefault("addr", addr)
            port = keyValueMap.getOrDefault("port", port)
            deviceid = keyValueMap.getOrDefault("deviceid", deviceid)
            devicekey = keyValueMap.getOrDefault("devicekey", devicekey)
            httpaddr = keyValueMap.getOrDefault("httpaddr", httpaddr)
            httpport = keyValueMap.getOrDefault("httpport", httpport)
            projid = keyValueMap.getOrDefault("projid", projid)
            regcode = keyValueMap.getOrDefault("regcode", regcode)
            type_code = keyValueMap.getOrDefault("type_code", type_code)
            co_address = keyValueMap.getOrDefault("co_address", co_address)
            password = keyValueMap.getOrDefault("password", password)
            taddress = keyValueMap.getOrDefault("taddress", taddress)
            hour_report = keyValueMap.getOrDefault("hour_report", hour_report)
            data_link = keyValueMap.getOrDefault("data_link", data_link)
            valid_day = keyValueMap.getOrDefault("valid_day", valid_day)
            reissue_time = keyValueMap.getOrDefault("reissue_time", reissue_time)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_DATA_CENTER
}