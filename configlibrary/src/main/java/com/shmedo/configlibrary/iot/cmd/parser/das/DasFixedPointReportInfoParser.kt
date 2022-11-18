package com.shmedo.configlibrary.iot.cmd.parser.das

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.das.DasFixedPointReportInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/11/18 <br></br>
 * 描述：     解析DAS  定时定点上报参数
 */
class DasFixedPointReportInfoParser : IOTResultParser<DasFixedPointReportInfo?> {
    override fun parse(result: String): DasFixedPointReportInfo? {
        val info = DasFixedPointReportInfo()
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
            info.type = keyValueMap.getOrDefault("type", "NullKey")
            info.timepoint = keyValueMap.getOrDefault("timepoint", "NullKey")
            info.timegap = keyValueMap.getOrDefault("timegap", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}

    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_DATA_REPORT_TYPE
    }
}