package com.shmedo.lib.cmd.base.md_cmd.parser.das

import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.model.das.BreakAlarmStatusInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/16
 * 描述： TODO
 */
class MDBreakAlarmStatusInfoParser: MDCommandParser<BreakAlarmStatusInfo> {
    override fun parseInstance(values: List<String>): BreakAlarmStatusInfo {
        return BreakAlarmStatusInfo().apply {
            status = values.getOrNull(0)?.substring(5) ?: status
        }
    }

    override fun commandType(): MDCommandType = MDCommandType.BREAK_ALARM_STATUS
}