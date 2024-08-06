package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDeviceInfo

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/10 <br/>
 * 描述：     TODO
 */
class MRDeviceInfoParser : IOTCommandParser<MRDeviceInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): MRDeviceInfo {
        when (keyValueMap["pages"]) {
            "1" -> {
                return MRDeviceInfo(
                    pages = "1",
                    label = keyValueMap["label"] ?: "1",
                    baseInfo = MRBaseInfoParser().parseInstance(keyValueMap)
                )
            }

            "2" -> when (keyValueMap["label"]) {
                "1" -> {
                    return MRDeviceInfo(
                        pages = "2",
                        label = "1",
                        communicationData = MRCommunicationDataParser().parseInstance(keyValueMap)
                    )
                }

                else -> {
                    return MRDeviceInfo(
                        pages = "2",
                        label = "2",
                        runningData = MRRunningDataParser().parseInstance(keyValueMap)
                    )
                }
            }
            "3" -> when (keyValueMap["label"]) {

                "1" -> {
                    return MRDeviceInfo(
                        pages = "3",
                        label = "1",
                        interfaceStatusInfo = MRInterfaceStatusInfoParser().parseInstance(keyValueMap)
                    )
                }

                else -> {
                    return MRDeviceInfo(
                        pages = "3",
                        label = "2",
                        ioStatusInfo = MRIOStatusInfoParser().parseInstance(keyValueMap)
                    )
                }
            }

            else -> {
                return MRDeviceInfo(
                    pages = "4",
                    label = keyValueMap["label"] ?: "1",
                    moduleStatusInfo = MRModuleStatusInfoParser().parseInstance(keyValueMap)
                )
            }

        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO
}