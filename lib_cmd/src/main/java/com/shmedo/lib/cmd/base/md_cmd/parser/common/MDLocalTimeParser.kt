package com.shmedo.lib.cmd.base.md_cmd.parser.common

import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.model.common.DeviceTimeInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/11
 * 描述： TODO
 */
class MDLocalTimeParser : MDCommandParser<DeviceTimeInfo> {

    override fun parseInstance(values: List<String>): DeviceTimeInfo {
        return DeviceTimeInfo().apply {
            values.getOrNull(0)?.let {
                val value = it.substring(5)
                val sb = StringBuilder()
                sb.append("20" + value.substring(0, 2))
                sb.append("-" + value.substring(2, 4))
                sb.append("-" + value.substring(4, 6))
                sb.append(" " + value.substring(6, 8))
                sb.append(":" + value.substring(8, 10))
                sb.append(":" + value.substring(10, 12))

                time = sb.toString()
            }
        }
    }


    override fun commandType(): MDCommandType = MDCommandType.LOCAL_TIME
}