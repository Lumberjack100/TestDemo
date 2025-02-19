package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port3CameraParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/20 <br/>
 * 描述：     RS485-3-摄像头参数解析器
 */
@IOTParser
class MRRS485Port3CameraParamParser: IOTCommandParser<MRRS485Port3CameraParam> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRRS485Port3CameraParam {
        return MRRS485Port3CameraParam().apply {
            index = keyValueMap.getOrDefault("index", index)
            status = keyValueMap.getOrDefault("status", status)
            switch = keyValueMap.getOrDefault("switch", switch)
            addr = keyValueMap.getOrDefault("addr", addr)
            baud = keyValueMap.getOrDefault("baud", baud)
            databit = keyValueMap.getOrDefault("databit", databit)
            parity = keyValueMap.getOrDefault("parity", parity)
            stopbit = keyValueMap.getOrDefault("stopbit", stopbit)
            type = keyValueMap.getOrDefault("type", type)
            resolut = keyValueMap.getOrDefault("resolut", resolut)
            quality = keyValueMap.getOrDefault("quality", quality)
            interval = keyValueMap.getOrDefault("interval", interval)
            workmode = keyValueMap.getOrDefault("workmode", workmode)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_MR_GET_RS485_PORT3_CAMERA_PARAM
} 