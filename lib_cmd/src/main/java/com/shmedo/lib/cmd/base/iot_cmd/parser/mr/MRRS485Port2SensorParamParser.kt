package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port2SensorParam

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/24 <br/>
 * 描述：     TODO
 */
class MRRS485Port2SensorParamParser : IOTCommandParser<MRRS485Port2SensorParam> {

    override fun parseInstance(keyValueMap: Map<String, String>): MRRS485Port2SensorParam {
        return MRRS485Port2SensorParam().apply {
            chl = keyValueMap.getOrDefault("chl", chl)
            model = keyValueMap.getOrDefault("model", model)
            swtoken = keyValueMap.getOrDefault("swtoken", swtoken)
            sensoraddr = keyValueMap.getOrDefault("sensoraddr", sensoraddr)
            sensortype = keyValueMap.getOrDefault("sensortype", sensortype)
            filtercnt = keyValueMap.getOrDefault("filtercnt", filtercnt)
            gateval = keyValueMap.getOrDefault("gateval", gateval)
            uplimit = keyValueMap.getOrDefault("uplimit", uplimit)
            lowlimit = keyValueMap.getOrDefault("lowlimit", lowlimit)
            corrvalue = keyValueMap.getOrDefault("corrvalue", corrvalue)
            calctype = keyValueMap.getOrDefault("calctype", calctype)
            kvalue = keyValueMap.getOrDefault("kvalue", kvalue)
            bvalue = keyValueMap.getOrDefault("bvalue", bvalue)
            r0value = keyValueMap.getOrDefault("r0value", r0value)
            t0value = keyValueMap.getOrDefault("t0value", t0value)
            l0value = keyValueMap.getOrDefault("l0value", l0value)
            lvalue = keyValueMap.getOrDefault("lvalue", lvalue)
            polyavalue = keyValueMap.getOrDefault("polyavalue", polyavalue)
            polybvalue = keyValueMap.getOrDefault("polybvalue", polybvalue)
            polycvalue = keyValueMap.getOrDefault("polycvalue", polycvalue)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR_PARAM
}