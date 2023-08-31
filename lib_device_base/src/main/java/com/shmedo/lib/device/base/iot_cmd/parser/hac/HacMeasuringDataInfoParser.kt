package com.shmedo.lib.device.base.iot_cmd.parser.hac

import android.text.TextUtils
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacHoleAreaDepthInfo
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMeasuringDataInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/11 <br></br>
 * 描述：    解析 AC10 数据测量配置参数
 */
class HacMeasuringDataInfoParser : IOTResultParser<HacMeasuringDataInfo?> {
    override fun parse(result: String): HacMeasuringDataInfo? {
        val info = HacMeasuringDataInfo()
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
            info.equipmodel = keyValueMap.getOrDefault("equipmodel", "NullKey")
            info.address = keyValueMap.getOrDefault("address", "NullKey")
            info.downwaitetime = keyValueMap.getOrDefault("downwaitetime", "NullKey")
            info.datatype = keyValueMap.getOrDefault("datatype", "NullKey")
            info.onewaytest = keyValueMap.getOrDefault("onewaytest", "NullKey")
            info.checkreverse = keyValueMap.getOrDefault("checkreverse", "NullKey")
            val value = keyValueMap["holelist"]
            val tempList =
                if (TextUtils.isEmpty(value)) null else GsonUtils.fromJson<List<HacHoleAreaDepthInfo>>(
                    value,
                    object : TypeToken<List<HacHoleAreaDepthInfo?>?>() {}.type
                )
            info.holelist = tempList
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_HAC_MD_GET_DATA_MEASURE_PARAM
    }
}