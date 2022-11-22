package com.shmedo.configlibrary.iot.cmd.parser.das

import com.blankj.utilcode.util.GsonUtils
import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.das.DasSubSensorStatusInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：     TODO
 */
class DasSubSensorStatusInfoParser : IOTResultParser<DasSubSensorStatusInfo?> {
    override fun parse(result: String): DasSubSensorStatusInfo? {
        var info: DasSubSensorStatusInfo? = null
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
            val status = keyValueMap["status"]
            info = GsonUtils.fromJson(status, DasSubSensorStatusInfo::class.java)
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS
    }
}