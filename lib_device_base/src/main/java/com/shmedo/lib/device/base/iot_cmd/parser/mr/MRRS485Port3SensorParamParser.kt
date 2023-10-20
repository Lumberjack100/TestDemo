package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port3SensorParam

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/20 <br/>
 * 描述：     TODO
 */
class MRRS485Port3SensorParamParser: IOTCommandParser<MRRS485Port3SensorParam> {

        override fun parseInstance(keyValueMap: Map<String, String>): MRRS485Port3SensorParam {
            return MRRS485Port3SensorParam().apply {
                device = keyValueMap.getOrDefault("device", device)
                switch = keyValueMap.getOrDefault("switch", switch)
                addr = keyValueMap.getOrDefault("addr", addr)
                baud = keyValueMap.getOrDefault("baud", baud)
                databit = keyValueMap.getOrDefault("databit", databit)
                paritybit = keyValueMap.getOrDefault("paritybit", paritybit)
                stopbit = keyValueMap.getOrDefault("stopbit", stopbit)
                status = keyValueMap.getOrDefault("status", status)
                duration = keyValueMap.getOrDefault("duration", duration)
                interval = keyValueMap.getOrDefault("interval", interval)
                svolt = keyValueMap.getOrDefault("svolt", svolt)
                bvolt = keyValueMap.getOrDefault("bvolt", bvolt)
                spower = keyValueMap.getOrDefault("spower", spower)
                lpower = keyValueMap.getOrDefault("lpower", lpower)
                volume = keyValueMap.getOrDefault("volume", volume)
                type = keyValueMap.getOrDefault("type", type)
                stime = keyValueMap.getOrDefault("stime", stime)
            }
        }

        override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_485_PORT3_SENSOR_PARAM
}