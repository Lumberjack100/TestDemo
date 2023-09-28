package com.shmedo.lib.device.base.iot_cmd.parser.common

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.common.WorkModeBean

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/21 <br/>
 * 描述：     TODO
 */
class WorkModeParser: IOTCommandParser<WorkModeBean> {
    override fun parseInstance(keyValueMap: Map<String, String>): WorkModeBean {
        return WorkModeBean().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.GET_WORK_MODE

}