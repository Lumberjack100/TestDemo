package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port1SensorParam

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/27 <br/>
 * 描述：     TODO
 */
class MRRS485Port1SensorParamParser : IOTCommandParser<MRRS485Port1SensorParam> {

    override fun parseInstance(keyValueMap: Map<String, String>): MRRS485Port1SensorParam {
        return MRRS485Port1SensorParam().apply {
            model = keyValueMap.getOrDefault("model", model)
            c_model = keyValueMap.getOrDefault("c_model", c_model)
            sensoraddr = keyValueMap.getOrDefault("sensoraddr", sensoraddr)
            num = keyValueMap.getOrDefault("num", num)
            swtoken = keyValueMap.getOrDefault("swtoken", swtoken)
            cmd = keyValueMap.getOrDefault("cmd", cmd)
            ratio = keyValueMap.getOrDefault("ratio", ratio)
            dataformat = keyValueMap.getOrDefault("dataformat", dataformat)
            calctype = keyValueMap.getOrDefault("calctype", calctype)
            gateval = keyValueMap.getOrDefault("gateval", gateval)
            uplimit = keyValueMap.getOrDefault("uplimit", uplimit)
            lowlimit = keyValueMap.getOrDefault("lowlimit", lowlimit)
            corrvalue = keyValueMap.getOrDefault("corrvalue", corrvalue)
            baud = keyValueMap.getOrDefault("baud", baud)
            databit = keyValueMap.getOrDefault("databit", databit)
            paritybit = keyValueMap.getOrDefault("paritybit", paritybit)
            stopbit = keyValueMap.getOrDefault("stopbit", stopbit)
            show = keyValueMap.getOrDefault("show", show)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR_PARAM

}