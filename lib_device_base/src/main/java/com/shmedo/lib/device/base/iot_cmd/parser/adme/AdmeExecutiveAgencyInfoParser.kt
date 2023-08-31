package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser

import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeExecutiveAgencyInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/29/20 <br></br>
 * 描述：  解析ADME执行机构参数
 */
class AdmeExecutiveAgencyInfoParser : IOTResultParser<AdmeExecutiveAgencyInfo?> {
    override fun parse(result: String): AdmeExecutiveAgencyInfo? {
        val info = AdmeExecutiveAgencyInfo()
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
            info.meastype = keyValueMap.getOrDefault("meastype", "NullKey")
            info.datatype = keyValueMap.getOrDefault("datatype", "NullKey")
            info.datareply = keyValueMap.getOrDefault("datareply", "NullKey")
            info.roundwaitetime = keyValueMap.getOrDefault("roundwaitetime", "NullKey")
            info.roundmeasinval = keyValueMap.getOrDefault("roundmeasinval", "NullKey")
            info.updatedate = keyValueMap.getOrDefault("updatedate", "NullKey")
            info.invalday = keyValueMap.getOrDefault("invalday", "NullKey")
            info.roundmeasstart = keyValueMap.getOrDefault("roundmeasstart", "NullKey")
            info.datainval = keyValueMap.getOrDefault("datainval", "NullKey")
            info.compensatetime = keyValueMap.getOrDefault("compensatetime", "NullKey")
            info.driveaddress = keyValueMap.getOrDefault("driveaddress", "NullKey")
            info.downspeed = keyValueMap.getOrDefault("downspeed", "NullKey")
            info.interdeep = keyValueMap.getOrDefault("interdeep", "NullKey")
            info.downwaitetime = keyValueMap.getOrDefault("downwaitetime", "NullKey")
            info.upspeed = keyValueMap.getOrDefault("upspeed", "NullKey")
            info.measpacing = keyValueMap.getOrDefault("measpacing", "NullKey")
            info.meaintertime = keyValueMap.getOrDefault("meaintertime", "NullKey")
            info.meabaseth = keyValueMap.getOrDefault("meabaseth", "NullKey")
            info.dwonblocked = keyValueMap.getOrDefault("dwonblocked", "NullKey")
            info.untimenum = keyValueMap.getOrDefault("untimenum", "NullKey")
            info.detectiontime = keyValueMap.getOrDefault("detectiontime", "NullKey")
            info.detectionstart = keyValueMap.getOrDefault("detectionstart", "NullKey")
            info.detectionend = keyValueMap.getOrDefault("detectionend", "NullKey")
            info.interval_compensation = keyValueMap.getOrDefault("interval_compensation", "NullKey")
            info.bottom_safe_distance = keyValueMap.getOrDefault("bottom_safe_distance", "NullKey")
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
        return IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY
    }
}