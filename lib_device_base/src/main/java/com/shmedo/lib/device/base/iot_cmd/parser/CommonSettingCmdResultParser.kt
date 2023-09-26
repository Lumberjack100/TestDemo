package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.CommonSettingCmdResult

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/9/2 <br></br>
 * 描述：     解析通用的设置指令响应结果
 */
class CommonSettingCmdResultParser : IOTResultParser<CommonSettingCmdResult> {
    override fun parseInstance(keyValueMap: Map<String, String>): CommonSettingCmdResult {
        return CommonSettingCmdResult().apply {
            isSucceed = keyValueMap["result"].equals("succ")
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.COMMON_SETTING_COMMAND
}