package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.MdSensorData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/13
 * @desc: 功能开关参数解析器
 *
 * 对应指令: md_sensor
 * 查询应答: $cmd=md_sensor&method=0&switch=1
 * 设置成功应答: $cmd=md_sensor&result=succ
 * 设置失败应答: $cmd=md_sensor&result=fail&reason=...
 */
@IOTParser
class MdSensorDataParser : IOTCommandParser<MdSensorData> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MdSensorData {
        return MdSensorData().apply {
            method = keyValueMap.getOrDefault("method", method)
            switch = keyValueMap.getOrDefault("switch", switch)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_SENSOR
}
