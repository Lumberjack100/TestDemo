package com.shmedo.lib.device.base.iot_cmd.parser.das

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.das.DasSensorStatusInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：     解析DAS 状态页面主传感器状态参数
 */
class DasSensorStatusInfoParser : IOTResultParser<List<DasSensorStatusInfo>?> {
    override fun parse(result: String): List<DasSensorStatusInfo>? {
        var sensorStatusInfoList: List<DasSensorStatusInfo>? = null
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
            val status = keyValueMap["status"]?: return null
            sensorStatusInfoList = MoshiUtil.fromJson<List<DasSensorStatusInfo>>(status)

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