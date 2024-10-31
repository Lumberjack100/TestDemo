package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.WorkModeBean
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/21 <br/>
 * 描述：     TODO
 */
@IOTParser
class WorkModeParser: IOTCommandParser<WorkModeBean> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): WorkModeBean {
        return WorkModeBean().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.GET_WORK_MODE

}