package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMotorMotionAngleInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/8/21 <br></br>
 * 描述：     解析电机实时运动数据
 */
class AdmeMotorMotionAngleInfoParser : IOTResultParser<AdmeMotorMotionAngleInfo?> {
    override fun parse(result: String): AdmeMotorMotionAngleInfo? {
        val info = AdmeMotorMotionAngleInfo()
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
            info.pulsenumber = keyValueMap.getOrDefault("pulsenumber", "NullKey")
            info.realmoveangle = keyValueMap.getOrDefault("realmoveangle", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE
    }
}