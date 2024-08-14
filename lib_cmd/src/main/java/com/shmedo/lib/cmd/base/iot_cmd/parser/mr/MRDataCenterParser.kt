package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDataCenterParam

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/28 <br/>
 * 描述：     数据中心解析器
 */
class MRDataCenterParser : IOTCommandParser<MRDataCenterParam> {

    override fun parseInstance(keyValueMap: Map<String, String>): MRDataCenterParam {
        return MRDataCenterParam().apply {
            centerid = keyValueMap.getOrDefault("centerid", centerid)
            switch = keyValueMap.getOrDefault("switch", switch)
            datanet = keyValueMap.getOrDefault("datanet", datanet)
            wirednet = keyValueMap.getOrDefault("wirednet", wirednet)
            line = keyValueMap.getOrDefault("line", line)
            level = keyValueMap.getOrDefault("level", level)
            type = keyValueMap.getOrDefault("type", type)
            addr = keyValueMap.getOrDefault("addr", addr)
            port = keyValueMap.getOrDefault("port", port)
            plattype = keyValueMap.getOrDefault("plattype", plattype)
            datatype = keyValueMap.getOrDefault("datatype", datatype)
            projid = keyValueMap.getOrDefault("projid", projid)
            deviceid = keyValueMap.getOrDefault("deviceid", deviceid)
            devicekey = keyValueMap.getOrDefault("devicekey", devicekey)
            regcode = keyValueMap.getOrDefault("regcode", regcode)
            httpaddr = keyValueMap.getOrDefault("httpaddr", httpaddr)
            httpport = keyValueMap.getOrDefault("httpport", httpport)
            keepalive = keyValueMap.getOrDefault("keepalive", keepalive)
            type_code = keyValueMap.getOrDefault("type_code", type_code)
            co_address = keyValueMap.getOrDefault("co_address", co_address)
            password = keyValueMap.getOrDefault("password", password)
            taddress = keyValueMap.getOrDefault("taddress", taddress)
            timed_report = keyValueMap.getOrDefault("timed_report", timed_report)
            hour_report = keyValueMap.getOrDefault("hour_report", hour_report)
            add_report = keyValueMap.getOrDefault("add_report", add_report)
            maintain_report = keyValueMap.getOrDefault("maintain_report", maintain_report)
            valid_day = keyValueMap.getOrDefault("valid_day", valid_day)
            reissue_time = keyValueMap.getOrDefault("reissue_time", reissue_time)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_DATA_CENTER
}
