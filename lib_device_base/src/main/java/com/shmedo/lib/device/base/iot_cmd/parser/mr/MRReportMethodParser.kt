package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandResponseParser
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRReportMethod

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/27 <br/>
 * 描述：     终端参数-上报方式
 */
class MRReportMethodParser: IOTCommandResponseParser<MRReportMethod> {

        override fun parseInstance(keyValueMap: Map<String, String>): MRReportMethod {
            return MRReportMethod().apply {
                type = keyValueMap.getOrDefault("type", type)
                interval = keyValueMap.getOrDefault("interval", interval)
                basis = keyValueMap.getOrDefault("basis", basis)
            }
        }

        override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_REPORT_METHOD
}