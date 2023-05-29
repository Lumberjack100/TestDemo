package com.shmedo.configlibrary.iot.cmd.parser.das

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/21 <br></br>
 * 描述：      解析DAS扩展传感器参数
 */
class DasExternalSensorInfoParser : IOTResultParser<DasExternalSensorInfo?> {
    override fun parse(result: String): DasExternalSensorInfo? {
        val info = DasExternalSensorInfo()
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
            info.index = keyValueMap.getOrDefault("index", "NullKey")
            info.type = keyValueMap.getOrDefault("type", "NullKey")
            info.addr = keyValueMap.getOrDefault("addr", "NullKey")
            info.threshold = keyValueMap.getOrDefault("threshold", "NullKey")
            info.corrval = keyValueMap.getOrDefault("corrval", "NullKey")
            info.spacing = keyValueMap.getOrDefault("spacing", "NullKey")
            info.holenum = keyValueMap.getOrDefault("holenum", "NullKey")
            info.tubealti = keyValueMap.getOrDefault("tubealti", "NullKey")
            info.ropelen = keyValueMap.getOrDefault("ropelen", "NullKey")
            info.poly_a = keyValueMap.getOrDefault("poly_a", "NullKey")
            info.poly_b = keyValueMap.getOrDefault("poly_b", "NullKey")
            info.poly_c = keyValueMap.getOrDefault("poly_c", "NullKey")
            info.temp_k = keyValueMap.getOrDefault("temp_k", "NullKey")
            info.temp_t0 = keyValueMap.getOrDefault("temp_t0", "NullKey")
            info.sens_k = keyValueMap.getOrDefault("sens_k", "NullKey")
            info.temp_b = keyValueMap.getOrDefault("temp_b", "NullKey")
            info.referval_f = keyValueMap.getOrDefault("referval_f", "NullKey")
            info.elastic_mod = keyValueMap.getOrDefault("elastic_mod", "NullKey")
            info.lsycsds = keyValueMap.getOrDefault("lsycsds", "NullKey")
            info.lsyysst = keyValueMap.getOrDefault("lsyysst", "NullKey")
            info.initvalx = keyValueMap.getOrDefault("initvalx", "NullKey")
            info.initvaly = keyValueMap.getOrDefault("initvaly", "NullKey")
            info.initvalz = keyValueMap.getOrDefault("initvalz", "NullKey")
            info.child_type = keyValueMap.getOrDefault("child_type", "NullKey")
            info.datatype = keyValueMap.getOrDefault("datatype", "NullKey")
            info.measinval = keyValueMap.getOrDefault("measinval", "NullKey")
            info.model_type = keyValueMap.getOrDefault("model_type", "NullKey")

            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR
    }
}