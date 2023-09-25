package com.shmedo.lib.device.base.iot_cmd.parser.das

import com.blankj.utilcode.util.GsonUtils
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.das.DasTemperatureAndHumidityStatusinfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：      解析DAS 状态页面温湿度状态参数
 */
class DasTemperatureAndHumidityStatusinfoParserOld :
    OldIOTResultParser<DasTemperatureAndHumidityStatusinfo?> {
    override fun parse(result: String): DasTemperatureAndHumidityStatusinfo? {
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
            GsonUtils.fromJson(status, DasTemperatureAndHumidityStatusinfo::class.java)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS
    }
}