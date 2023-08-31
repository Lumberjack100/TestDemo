package com.shmedo.lib.device.base.iot_cmd.parser.hac

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMotorMotionDistanceInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/25 <br></br>
 * 描述：      解析电机实时运动数据
 */
class HacMotorMotionDistanceInfoParser : IOTResultParser<HacMotorMotionDistanceInfo?> {
    override fun parse(result: String): HacMotorMotionDistanceInfo? {
        val info = HacMotorMotionDistanceInfo()
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
            info.realmovedistance = keyValueMap.getOrDefault("realmovedistance", "NullKey")
            info.realholedepth = keyValueMap.getOrDefault("realholedepth", "NullKey")
            info.abndiasis = keyValueMap.getOrDefault("abndiasis", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PULSE
    }
}