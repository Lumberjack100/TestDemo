package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/2/1
 * 描述： TODO
 */
@IOTParser
class DasExternalSensorInfoParser : IOTCommandParser<DasExternalSensorInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DasExternalSensorInfo {
        return DasExternalSensorInfo().apply {
            index = keyValueMap.getOrDefault("index", index)
            type = keyValueMap.getOrDefault("type", IOTConstants.NULL_KEY)
            addr = keyValueMap.getOrDefault("addr", IOTConstants.NULL_KEY)
            threshold = keyValueMap.getOrDefault("threshold", IOTConstants.NULL_KEY)
            corrval = keyValueMap.getOrDefault("corrval", IOTConstants.NULL_KEY)
            spacing = keyValueMap.getOrDefault("spacing", IOTConstants.NULL_KEY)
            holenum = keyValueMap.getOrDefault("holenum", IOTConstants.NULL_KEY)
            tubealti = keyValueMap.getOrDefault("tubealti", IOTConstants.NULL_KEY)
            ropelen = keyValueMap.getOrDefault("ropelen", IOTConstants.NULL_KEY)
            poly_a = keyValueMap.getOrDefault("poly_a", IOTConstants.NULL_KEY)
            poly_b = keyValueMap.getOrDefault("poly_b", IOTConstants.NULL_KEY)
            poly_c = keyValueMap.getOrDefault("poly_c", IOTConstants.NULL_KEY)
            temp_k = keyValueMap.getOrDefault("temp_k", IOTConstants.NULL_KEY)
            temp_t0 = keyValueMap.getOrDefault("temp_t0", IOTConstants.NULL_KEY)
            sens_k = keyValueMap.getOrDefault("sens_k", IOTConstants.NULL_KEY)
            temp_b = keyValueMap.getOrDefault("temp_b", IOTConstants.NULL_KEY)
            referval_f = keyValueMap.getOrDefault("referval_f", IOTConstants.NULL_KEY)
            elastic_mod = keyValueMap.getOrDefault("elastic_mod", IOTConstants.NULL_KEY)
            lsycsds = keyValueMap.getOrDefault("lsycsds", IOTConstants.NULL_KEY)
            lsyysst = keyValueMap.getOrDefault("lsyysst", IOTConstants.NULL_KEY)
            initvalx = keyValueMap.getOrDefault("initvalx", IOTConstants.NULL_KEY)
            initvaly = keyValueMap.getOrDefault("initvaly", IOTConstants.NULL_KEY)
            initvalz = keyValueMap.getOrDefault("initvalz", IOTConstants.NULL_KEY)
            child_type = keyValueMap.getOrDefault("child_type", IOTConstants.NULL_KEY)
            datatype = keyValueMap.getOrDefault("datatype", IOTConstants.NULL_KEY)
            measinval = keyValueMap.getOrDefault("measinval", IOTConstants.NULL_KEY)
            model_type = keyValueMap.getOrDefault("model_type", IOTConstants.NULL_KEY)
            initval = keyValueMap.getOrDefault("initval", IOTConstants.NULL_KEY)
            caddr = keyValueMap.getOrDefault("caddr", IOTConstants.NULL_KEY)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR
}