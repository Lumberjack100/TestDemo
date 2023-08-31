package com.shmedo.lib.device.base.iot_cmd.parser.das

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.das.DasDataReportInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/20 <br></br>
 * 描述：       解析DAS  数据上报时间参数
 */
class DasDataReportInfoParser : IOTResultParser<DasDataReportInfo?> {
    override fun parse(result: String): DasDataReportInfo? {
        val info = DasDataReportInfo()
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
            info.report_intv = keyValueMap.getOrDefault("report_intv", "NullKey")
            info.plus_intv = keyValueMap.getOrDefault("plus_intv", "NullKey")
            info.plus_count = keyValueMap.getOrDefault("plus_count", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.MD_GET_DATA_REPORT_TIME
    }
}