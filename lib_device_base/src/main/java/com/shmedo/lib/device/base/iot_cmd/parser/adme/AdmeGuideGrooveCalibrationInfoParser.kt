package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser

import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeGuideGrooveCalibrationInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/8/21 <br></br>
 * 描述：     解析ADME导槽校准配置参数
 */
class AdmeGuideGrooveCalibrationInfoParser : IOTResultParser<AdmeGuideGrooveCalibrationInfo?> {
    override fun parse(result: String): AdmeGuideGrooveCalibrationInfo? {
        val info = AdmeGuideGrooveCalibrationInfo()
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
            info.morunstate = keyValueMap.getOrDefault("morunstate", "NullKey")
            info.movementway = keyValueMap.getOrDefault("movementway", "NullKey")
            info.motorspeed = keyValueMap.getOrDefault("motorspeed", "NullKey")
            info.movePulse = keyValueMap.getOrDefault("movepulse", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION
    }
}