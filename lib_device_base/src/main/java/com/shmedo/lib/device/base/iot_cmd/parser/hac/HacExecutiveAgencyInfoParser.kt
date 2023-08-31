package com.shmedo.lib.device.base.iot_cmd.parser.hac

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacExecutiveAgencyInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/29 <br></br>
 * 描述：     TODO
 */
class HacExecutiveAgencyInfoParser : IOTResultParser<HacExecutiveAgencyInfo?> {
    override fun parse(result: String): HacExecutiveAgencyInfo? {
        val info = HacExecutiveAgencyInfo()
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
            info.datatype = keyValueMap.getOrDefault("datatype", "NullKey")
            info.datareply = keyValueMap.getOrDefault("datareply", "NullKey")
            info.datainval = keyValueMap.getOrDefault("datainval", "NullKey")
            info.compensatetime = keyValueMap.getOrDefault("compensatetime", "NullKey")
            info.driveaddress = keyValueMap.getOrDefault("driveaddress", "NullKey")
            info.downspeed = keyValueMap.getOrDefault("downspeed", "NullKey")
            info.downwaitetime = keyValueMap.getOrDefault("downwaitetime", "NullKey")
            info.upspeed = keyValueMap.getOrDefault("upspeed", "NullKey")
            info.measpacing = keyValueMap.getOrDefault("measpacing", "NullKey")
            info.meaintertime = keyValueMap.getOrDefault("meaintertime", "NullKey")
            info.interval_compensation =
                keyValueMap.getOrDefault("interval_compensation", "NullKey")
            info.interval_fitting = keyValueMap.getOrDefault("interval_fitting", "NullKey")
            info.point_offset = keyValueMap.getOrDefault("point_offset", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_HAC_MD_GET_EXECUTIVE_AGENCY
    }
}