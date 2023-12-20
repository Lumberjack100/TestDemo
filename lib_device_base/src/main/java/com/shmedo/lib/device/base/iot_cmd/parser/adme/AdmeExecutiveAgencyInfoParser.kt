package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeExecutiveAgencyInfo

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/19
 *
 * 描述： TODO
 *
 *
 */
class AdmeExecutiveAgencyInfoParser : IOTCommandParser<AdmeExecutiveAgencyInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeExecutiveAgencyInfo {
        return AdmeExecutiveAgencyInfo().apply {
            meastype = keyValueMap.getOrDefault("meastype", meastype)
            datatype = keyValueMap.getOrDefault("datatype", datatype)
            datareply = keyValueMap.getOrDefault("datareply", datareply)
            roundwaitetime = keyValueMap.getOrDefault("roundwaitetime", roundwaitetime)
            roundmeasinval = keyValueMap.getOrDefault("roundmeasinval", roundmeasinval)
            updatedate = keyValueMap.getOrDefault("updatedate", updatedate)
            invalday = keyValueMap.getOrDefault("invalday", invalday)
            roundmeasstart = keyValueMap.getOrDefault("roundmeasstart", roundmeasstart)
            datainval = keyValueMap.getOrDefault("datainval", datainval)
            compensatetime = keyValueMap.getOrDefault("compensatetime", compensatetime)
            driveaddress = keyValueMap.getOrDefault("driveaddress", driveaddress)
            downspeed = keyValueMap.getOrDefault("downspeed", downspeed)
            interdeep = keyValueMap.getOrDefault("interdeep", interdeep)
            downwaitetime = keyValueMap.getOrDefault("downwaitetime", downwaitetime)
            upspeed = keyValueMap.getOrDefault("upspeed", upspeed)
            measpacing = keyValueMap.getOrDefault("measpacing", measpacing)
            meaintertime = keyValueMap.getOrDefault("meaintertime", meaintertime)
            meabaseth = keyValueMap.getOrDefault("meabaseth", meabaseth)
            dwonblocked = keyValueMap.getOrDefault("dwonblocked", dwonblocked)
            untimenum = keyValueMap.getOrDefault("untimenum", untimenum)
            detectiontime = keyValueMap.getOrDefault("detectiontime", detectiontime)
            detectionstart = keyValueMap.getOrDefault("detectionstart", detectionstart)
            detectionend = keyValueMap.getOrDefault("detectionend", detectionend)
            interval_compensation = keyValueMap.getOrDefault("interval_compensation", interval_compensation)
            bottom_safe_distance = keyValueMap.getOrDefault("bottom_safe_distance", bottom_safe_distance)
            interval_fitting = keyValueMap.getOrDefault("interval_fitting", interval_fitting)
            point_offset = keyValueMap.getOrDefault("point_offset", point_offset)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY

}