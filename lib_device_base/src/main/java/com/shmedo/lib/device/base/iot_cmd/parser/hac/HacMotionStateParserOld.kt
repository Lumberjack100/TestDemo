package com.shmedo.lib.device.base.iot_cmd.parser.hac

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMotionState

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/15 <br></br>
 * 描述：     解析 HAC 电机运动状态
 */
class HacMotionStateParserOld :
    OldIOTResultParser<HacMotionState?> {
    override fun parse(result: String): HacMotionState? {
        val info = HacMotionState()
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
            info.abndiasis = keyValueMap.getOrDefault("abndiasis", "NullKey")
            info.measmode = keyValueMap.getOrDefault("measmode", "NullKey")
            info.motorinfo = keyValueMap.getOrDefault("motorinfo", "NullKey")
            info.measpoint = keyValueMap.getOrDefault("measpoint", "NullKey")
            info.waittime = keyValueMap.getOrDefault("waittime", "NullKey")
            info.incvoltage = keyValueMap.getOrDefault("incvoltage", "NullKey")
            info.driveinputv = keyValueMap.getOrDefault("driveinputv", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
    }
}