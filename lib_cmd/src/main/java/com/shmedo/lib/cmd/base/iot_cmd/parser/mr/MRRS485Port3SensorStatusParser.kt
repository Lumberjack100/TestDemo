package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port3SensorStatus

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/19 <br/>
 * 描述：     TODO
 */
class MRRS485Port3SensorStatusParser : IOTCommandParser<MRRS485Port3SensorStatus> {

    override fun parseInstance(keyValueMap: Map<String, String>): MRRS485Port3SensorStatus {
        return MRRS485Port3SensorStatus().apply {
            solarid = keyValueMap.getOrDefault("solarid", solarid)
            solarstatus = keyValueMap.getOrDefault("solarstatus", solarstatus)
            ysid = keyValueMap.getOrDefault("ysid", ysid)
            ysstatus = keyValueMap.getOrDefault("ysstatus", ysstatus)
            ledid = keyValueMap.getOrDefault("ledid", ledid)
            ledstatus = keyValueMap.getOrDefault("ledstatus", ledstatus)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_STATUS
}