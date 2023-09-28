package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRScreenParam

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/27 <br/>
 * 描述：     TODO
 */
class MRScreenParamParser: IOTCommandParser<MRScreenParam> {

            override fun parseInstance(keyValueMap: Map<String, String>): MRScreenParam {
                return MRScreenParam().apply {
                    interval = keyValueMap.getOrDefault("interval", interval)
                    otime = keyValueMap.getOrDefault("otime", otime)
                    ptime = keyValueMap.getOrDefault("ptime", ptime)
                    bproport = keyValueMap.getOrDefault("bproport", bproport)
                }
            }

            override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_SCREEN_PARAM
}