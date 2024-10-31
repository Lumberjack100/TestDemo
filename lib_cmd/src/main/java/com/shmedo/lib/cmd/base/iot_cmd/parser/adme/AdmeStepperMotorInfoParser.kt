package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeStepperMotorInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/14
 *
 * 描述： TODO
 *
 *
 */
@IOTParser
class AdmeStepperMotorInfoParser: IOTCommandParser<AdmeStepperMotorInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeStepperMotorInfo {
        return AdmeStepperMotorInfo().apply {
            posnegtest = keyValueMap.getOrDefault("posnegtest", posnegtest)
            absprsion = keyValueMap.getOrDefault("absprsion", absprsion)
            movspeed = keyValueMap.getOrDefault("movspeed", movspeed)
            movesm = keyValueMap.getOrDefault("movesm", movesm)
        }
    }
    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_STEPPER_MOTOR
}