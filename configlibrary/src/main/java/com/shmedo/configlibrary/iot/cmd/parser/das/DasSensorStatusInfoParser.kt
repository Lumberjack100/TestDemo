package com.shmedo.configlibrary.iot.cmd.parser.das

import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.das.DasSensorStatusInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：     解析DAS 状态页面主传感器状态参数
 */
class DasSensorStatusInfoParser : IOTResultParser<List<DasSensorStatusInfo?>?> {
    override fun parse(result: String): List<DasSensorStatusInfo?>? {
        var sensorStatusInfoList: List<DasSensorStatusInfo?>? = null
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
            sensorStatusInfoList = GsonUtils.fromJson(
                status,
                object : TypeToken<List<DasSensorStatusInfo?>?>() {}.type
            )
            sensorStatusInfoList
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_SENSOR_STATUS
    }
}