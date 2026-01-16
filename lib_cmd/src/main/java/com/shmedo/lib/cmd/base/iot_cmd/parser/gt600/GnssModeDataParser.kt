package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.GnssModeData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/14
 * @desc: GNSS工作模式（站点类型）解析器
 *
 * 对应指令: md_gnssmode
 * 查询应答: $cmd=md_gnssmode&method=0&station=1
 * 设置成功应答: $cmd=md_gnssmode&method=1&result=succ
 * 设置失败应答: $cmd=md_gnssmode&method=1&result=fail&reason=...
 */
@IOTParser
class GnssModeDataParser : IOTCommandParser<GnssModeData> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): GnssModeData {
        return GnssModeData().apply {
            method = keyValueMap.getOrDefault("method", method)
            station = keyValueMap.getOrDefault("station", station)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GNSSMODE
}
