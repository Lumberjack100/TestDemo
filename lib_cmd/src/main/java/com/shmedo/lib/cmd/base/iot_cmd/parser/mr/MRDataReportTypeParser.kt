package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDataReportType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/27 <br/>
 * 描述：     终端参数-上报方式
 */
@IOTParser
class MRDataReportTypeParser: IOTCommandParser<MRDataReportType> {

        override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRDataReportType {
            return MRDataReportType().apply {
                type = keyValueMap.getOrDefault("type", type)
                interval = keyValueMap.getOrDefault("interval", interval)
                basis = keyValueMap.getOrDefault("basis", basis)
            }
        }

        override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_REPORT_TYPE
}