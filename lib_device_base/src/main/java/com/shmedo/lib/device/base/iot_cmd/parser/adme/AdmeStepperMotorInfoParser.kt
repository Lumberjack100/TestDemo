package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeStepperMotorInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：    解析ADME步进电机参数
 */
class AdmeStepperMotorInfoParser : IOTResultParser<AdmeStepperMotorInfo?> {
    override fun parse(result: String): AdmeStepperMotorInfo? {
        val info = AdmeStepperMotorInfo()
        return try {
            val keyValues = result.split("&").toTypedArray()
            val keyValueMap = HashMap<String, String>()
            for (keyValue in keyValues) {
                val strs = keyValue.split("=").toTypedArray()
                if (strs.size < 2) {
                    keyValueMap[strs[0]] = ""
                } else {
                    keyValueMap[strs[0]] = strs[1]
                }
            }
            info.posnegtest = keyValueMap.getOrDefault("posnegtest", "NullKey")
            info.absprsion = keyValueMap.getOrDefault("absprsion", "NullKey")
            info.movspeed = keyValueMap.getOrDefault("movspeed", "NullKey")
            info.movesm = keyValueMap.getOrDefault("movesm", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_STEPPER_MOTOR
    }
}