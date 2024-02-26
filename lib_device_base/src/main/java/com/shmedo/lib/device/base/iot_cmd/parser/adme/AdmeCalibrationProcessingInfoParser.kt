package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeCalibrationProcessingInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/2/26
 * 描述： TODO
 */
class AdmeCalibrationProcessingInfoParser: IOTCommandParser<AdmeCalibrationProcessingInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeCalibrationProcessingInfo {
        return AdmeCalibrationProcessingInfo().apply {
            rivswitch = keyValueMap.getOrDefault("rivswitch", rivswitch)
            zerodiffer = keyValueMap.getOrDefault("zerodiffer", zerodiffer)
            posterrorint = keyValueMap.getOrDefault("posterrorint", posterrorint)
            rtrynum = keyValueMap.getOrDefault("rtrynum", rtrynum)
            kcswitch = keyValueMap.getOrDefault("kcswitch", kcswitch)
            kthreshold = keyValueMap.getOrDefault("kthreshold", kthreshold)
            methreshold = keyValueMap.getOrDefault("methreshold", methreshold)
            ktrynum = keyValueMap.getOrDefault("ktrynum", ktrynum)
            dpswitch = keyValueMap.getOrDefault("dpswitch", dpswitch)
            accudiff = keyValueMap.getOrDefault("accudiff", accudiff)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_CALIBRATION_PROCESSING
}