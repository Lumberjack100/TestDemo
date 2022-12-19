package com.shmedo.configlibrary.iot.cmd.parser.adme

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.adme.AdmeStepperMotorRelayModeInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/1/25 <br></br>
 * 描述：     ADME 步进电机继电器使能状态
 */
class AdmeStepperMotorRelayModeInfoParser : IOTResultParser<AdmeStepperMotorRelayModeInfo?> {
    override fun parse(result: String): AdmeStepperMotorRelayModeInfo? {
        val info = AdmeStepperMotorRelayModeInfo()
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
            info.mode = keyValueMap.getOrDefault("mode", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_STEPPER_MOTOR_RELAY_MODE
    }
}