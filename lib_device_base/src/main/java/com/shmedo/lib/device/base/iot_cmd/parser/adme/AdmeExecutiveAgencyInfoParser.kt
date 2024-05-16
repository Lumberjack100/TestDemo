package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeExecutiveAgencyInfo
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants

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
            meastype = keyValueMap.getOrDefault("meastype", IOTConstants.NULL_KEY)
            datatype = keyValueMap.getOrDefault("datatype", IOTConstants.NULL_KEY)
            datareply = keyValueMap.getOrDefault("datareply", IOTConstants.NULL_KEY)
            roundwaitetime = keyValueMap.getOrDefault("roundwaitetime", IOTConstants.NULL_KEY)
            roundmeasinval = keyValueMap.getOrDefault("roundmeasinval", IOTConstants.NULL_KEY)
            updatedate = keyValueMap.getOrDefault("updatedate", IOTConstants.NULL_KEY)
            invalday = keyValueMap.getOrDefault("invalday", IOTConstants.NULL_KEY)
            roundmeasstart = keyValueMap.getOrDefault("roundmeasstart", IOTConstants.NULL_KEY)
            datainval = keyValueMap.getOrDefault("datainval", IOTConstants.NULL_KEY)
            compensatetime = keyValueMap.getOrDefault("compensatetime", IOTConstants.NULL_KEY)
            driveaddress = keyValueMap.getOrDefault("driveaddress", IOTConstants.NULL_KEY)
            downspeed = keyValueMap.getOrDefault("downspeed", IOTConstants.NULL_KEY)
            interdeep = keyValueMap.getOrDefault("interdeep", IOTConstants.NULL_KEY)
            downwaitetime = keyValueMap.getOrDefault("downwaitetime", IOTConstants.NULL_KEY)
            upspeed = keyValueMap.getOrDefault("upspeed", IOTConstants.NULL_KEY)
            pzspeed = keyValueMap.getOrDefault("pzspeed", IOTConstants.NULL_KEY)
            measpacing = keyValueMap.getOrDefault("measpacing", IOTConstants.NULL_KEY)
            meaintertime = keyValueMap.getOrDefault("meaintertime", IOTConstants.NULL_KEY)
            meabaseth = keyValueMap.getOrDefault("meabaseth", IOTConstants.NULL_KEY)
            dwonblocked = keyValueMap.getOrDefault("dwonblocked", IOTConstants.NULL_KEY)
            untimenum = keyValueMap.getOrDefault("untimenum", IOTConstants.NULL_KEY)
            detectiontime = keyValueMap.getOrDefault("detectiontime", IOTConstants.NULL_KEY)
            detectionstart = keyValueMap.getOrDefault("detectionstart", IOTConstants.NULL_KEY)
            detectionend = keyValueMap.getOrDefault("detectionend", IOTConstants.NULL_KEY)
            interval_compensation = keyValueMap.getOrDefault("interval_compensation", IOTConstants.NULL_KEY)
            bottom_safe_distance = keyValueMap.getOrDefault("bottom_safe_distance", IOTConstants.NULL_KEY)
            interval_fitting = keyValueMap.getOrDefault("interval_fitting", IOTConstants.NULL_KEY)
            point_offset = keyValueMap.getOrDefault("point_offset", IOTConstants.NULL_KEY)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY
}