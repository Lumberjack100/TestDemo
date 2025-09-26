package com.shmedo.lib.cmd.base.md_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataReportType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser

/**
 * 创建者：gonghe
 * 创建时间：2024/1/8
 * 描述：数据上吧配置参数
 */
class MDDasDataReportInfoParser : MDCommandParser<DataReportType> {
    override fun parseInstance(values: List<String>): DataReportType {
        return DataReportType().apply {
            type = values.getOrNull(1) ?: type
            timepoint = values.getOrNull(2) ?: timepoint
            timemin = values.getOrNull(3) ?: timemin
            timegap = values.getOrNull(4) ?: timegap
        }
    }

    override fun commandType(): MDCommandType = MDCommandType.DATA_REPORT_TYPE

}