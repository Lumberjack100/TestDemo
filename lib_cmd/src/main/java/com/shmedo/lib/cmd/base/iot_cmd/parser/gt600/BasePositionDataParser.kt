package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.BasePositionData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: GT600 基站位置信息解析器
 *
 * 对应指令: md_getbaseposition
 * 应答示例: $cmd=md_getbaseposition&mode=1&lat=0.000000&lon=0.000000&alt=0.000000
 */
@IOTParser
class BasePositionDataParser : IOTCommandParser<BasePositionData> {

    /**
     * 解析键值对映射为 BasePositionData 对象
     * 
     * @param keyValueMap 从指令响应中解析出的键值对
     * @return 解析后的 BasePositionData 对象
     */
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): BasePositionData {
        return BasePositionData().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
            lat = keyValueMap.getOrDefault("lat", lat)
            lon = keyValueMap.getOrDefault("lon", lon)
            alt = keyValueMap.getOrDefault("alt", alt)
        }
    }

    /** 对应的指令类型 */
    override val commandType: IOTCommandType = IOTCommandType.MD_GET_BASE_POSITION
}
