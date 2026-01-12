package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.Net4GUseData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: 4G 网络使用开关响应解析器
 *
 * 对应指令: md_getnet4guse
 * 应答示例: $cmd=md_getnet4guse&use=1
 */
@IOTParser
class Net4GUseDataParser : IOTCommandParser<Net4GUseData> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): Net4GUseData {
        return Net4GUseData().apply {
            use = keyValueMap.getOrDefault("use", use)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_NET_4G_USE
}
