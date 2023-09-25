package com.shmedo.lib.device.base.iot_cmd.parser.hac

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacHoleAreaDepthInfo
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMeasuringHoleDepthInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/12 <br></br>
 * 描述：      解析 AC10 孔深测量参数
 */
class HacMeasuringHoleDepthInfoParserOld :
    OldIOTResultParser<HacMeasuringHoleDepthInfo?> {
    override fun parse(result: String): HacMeasuringHoleDepthInfo? {
        val hacMeasuringHoleDepthInfo = HacMeasuringHoleDepthInfo()
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
            hacMeasuringHoleDepthInfo.address = keyValueMap.getOrDefault("address", "NullKey")
            hacMeasuringHoleDepthInfo.lowtbtss = keyValueMap.getOrDefault("lowtbtss", "NullKey")
            val value = keyValueMap["holelist"]?: return null
            hacMeasuringHoleDepthInfo.holelist = MoshiUtil.fromJson<List<HacHoleAreaDepthInfo>>(value)
            hacMeasuringHoleDepthInfo
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PARAM
    }
}