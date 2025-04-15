package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDataCenterStatus
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/28 <br/>
 * 描述：    获取数据链路状态
 */
@IOTParser
class MRDataCenterStatusParser : IOTCommandParser<MRDataCenterStatus> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRDataCenterStatus {
        return MRDataCenterStatus().apply {
            status1 = keyValueMap.getOrDefault("status1", status1)
            status2 = keyValueMap.getOrDefault("status2", status2)
            status3 = keyValueMap.getOrDefault("status3", status3)
            status4 = keyValueMap.getOrDefault("status4", status4)
            status5 = keyValueMap.getOrDefault("status5", status5)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_DATA_CENTER_STATUS
}