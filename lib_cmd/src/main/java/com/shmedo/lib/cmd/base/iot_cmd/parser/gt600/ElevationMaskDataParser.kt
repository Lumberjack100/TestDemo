package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.ElevationMaskData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/13
 * @desc: 截至高度角参数解析器
 *
 * 对应指令: md_elevation_mask
 * 查询应答: $cmd=md_elevation_mask&method=0&angle=15
 * 设置成功应答: $cmd=md_elevation_mask&result=succ
 * 设置失败应答: $cmd=md_elevation_mask&result=fail&reason=...
 */
@IOTParser
class ElevationMaskDataParser : IOTCommandParser<ElevationMaskData> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): ElevationMaskData {
        return ElevationMaskData().apply {
            method = keyValueMap.getOrDefault("method", method)
            angle = keyValueMap.getOrDefault("angle", angle)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_ELEVATION_MASK
}
