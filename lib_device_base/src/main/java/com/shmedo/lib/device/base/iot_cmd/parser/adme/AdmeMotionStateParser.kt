package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMotionState

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：      解析ADME运行状态
 */
class AdmeMotionStateParser : IOTResultParser<AdmeMotionState?> {
    override fun parse(result: String): AdmeMotionState? {
        val info = AdmeMotionState()
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
            info.motionstate = keyValueMap.getOrDefault("motionstate", "NullKey")
            info.inctiondis = keyValueMap.getOrDefault("inctiondis", "NullKey")
            info.measmode = keyValueMap.getOrDefault("measmode", "NullKey")
            info.motorinfo = keyValueMap.getOrDefault("motorinfo", "NullKey")
            info.measpoint = keyValueMap.getOrDefault("measpoint", "NullKey")
            info.waittime = keyValueMap.getOrDefault("waittime", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_MD_GET_MOTION_STATE
    }
}