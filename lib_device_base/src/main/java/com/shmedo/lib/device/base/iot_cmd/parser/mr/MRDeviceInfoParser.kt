package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDeviceInfo

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/10 <br/>
 * 描述：     TODO
 */
class MRDeviceInfoParser : IOTCommandParser<MRDeviceInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): MRDeviceInfo {
        when (keyValueMap["pages"]) {
            "1" -> {
                return MRDeviceInfo(baseInfo = MRBaseInfoParser().parseInstance(keyValueMap))
            }

            else -> {
                return MRDeviceInfo(
                    moduleStatusInfo = MRModuleStatusInfoParser().parseInstance(keyValueMap)
                )
            }

        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO
}