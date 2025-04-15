package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRAlarmModuleParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 报警模块参数解析器
 */
@IOTParser
class MRAlarmModuleParamParser : IOTCommandParser<MRAlarmModuleParam> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRAlarmModuleParam {
        return MRAlarmModuleParam().apply {
            index = keyValueMap.getOrDefault("index", index)
            switch = keyValueMap.getOrDefault("switch", switch)
            relay = keyValueMap.getOrDefault("relay", relay)
            warnlevel = keyValueMap.getOrDefault("warnlevel", warnlevel)
            holdtime1 = keyValueMap.getOrDefault("holdtime1", holdtime1)
            holdtime2 = keyValueMap.getOrDefault("holdtime2", holdtime2)
            holdtime3 = keyValueMap.getOrDefault("holdtime3", holdtime3)
            holdtime4 = keyValueMap.getOrDefault("holdtime4", holdtime4)
            gaptime1 = keyValueMap.getOrDefault("gaptime1", gaptime1)
            gaptime2 = keyValueMap.getOrDefault("gaptime2", gaptime2)
            gaptime3 = keyValueMap.getOrDefault("gaptime3", gaptime3)
            gaptime4 = keyValueMap.getOrDefault("gaptime4", gaptime4)
            voiceindex1 = keyValueMap.getOrDefault("voiceindex1", voiceindex1)
            voiceindex2 = keyValueMap.getOrDefault("voiceindex2", voiceindex2)
            voiceindex3 = keyValueMap.getOrDefault("voiceindex3", voiceindex3)
            voiceindex4 = keyValueMap.getOrDefault("voiceindex4", voiceindex4)
            voiceindex5 = keyValueMap.getOrDefault("voiceindex5", voiceindex5)
            respindex1 = keyValueMap.getOrDefault("respindex1", respindex1)
            respindex2 = keyValueMap.getOrDefault("respindex2", respindex2)
            respindex3 = keyValueMap.getOrDefault("respindex3", respindex3)
            respindex4 = keyValueMap.getOrDefault("respindex4", respindex4)
            respindex5 = keyValueMap.getOrDefault("respindex5", respindex5)
            cleargaptime = keyValueMap.getOrDefault("cleargaptime", cleargaptime)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_ALARM_MODULE
}