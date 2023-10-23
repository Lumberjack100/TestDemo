package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRSensorStatus

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/17 <br/>
 * 描述：     TODO
 */
class MR485Port2SensorStatusParser: IOTCommandParser<MRSensorStatus> {
    override fun parseInstance(keyValueMap: Map<String, String>): MRSensorStatus {
        return MRSensorStatus().apply {
            model = keyValueMap.getOrDefault("model", model)
            sta = keyValueMap.getOrDefault("sta", sta)
            chl = keyValueMap.getOrDefault("chl", chl)
        }
    }
    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR
}