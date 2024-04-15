package com.shmedo.lib.device.base.md_cmd.parser.das

import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.model.das.DeviceStatusInfoThree

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 *
 * 应答:$$043,(1),(2),(3),(4),(5)<br/>
 * （1）SN号<br/>
 * （2）采集器型号<br/>
 * （3）采集器地址(当采集器地址为0时，关闭采集功能)<br/>
 * （4）传感器状态，用冒号分隔的字符串<br/>
 * ①:②:③，其中 ①：传感器地址，②：传感器状态，0正常，1异常，③：传感器数据<br/>
 * （5）传感器状态，和（2）格式相同，<br/>
 * 注：传感器状态可能有很多个，有接入传感器个数决定。<br/>
 * $$043,224297L,0,1,0:1:0.0
 */
class MDDeviceStatusInfoThreeParser : MDCommandParser<DeviceStatusInfoThree> {
    override fun parseInstance(values: List<String>): DeviceStatusInfoThree {
        return DeviceStatusInfoThree(
            snNumber = values.getOrNull(1) ?: "",
            collectorModel = values.getOrNull(2) ?: "",
            collectorAddress = values.getOrNull(3) ?: "",
            sensorStatus = values.subList(4, values.size)
        )
    }

    override fun commandType(): MDCommandType = MDCommandType.QUERY_DAS_STATUS_3
}