package com.shmedo.lib.device.base.iot_cmd.parser

import android.text.TextUtils
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.CommonSettingCmdResult

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/9/2 <br></br>
 * 描述：     解析通用的设置指令响应结果
 */
class CommonSettingCmdResultParser : IOTResultParser<CommonSettingCmdResult> {
    override fun parse(result: String): CommonSettingCmdResult {
        val commonSettingCmdResult = CommonSettingCmdResult()
        val strs = result.split("&").toTypedArray()
        for (ss in strs) {
            if (ss.endsWith("=succ")) {
                commonSettingCmdResult.isSucceed = true
                continue
            }
            if (ss.endsWith("=fail")) {
                commonSettingCmdResult.isSucceed = false
                continue
            }
            if (ss.startsWith("reason=")) {
                val value = ss.replace("reason=", "")
                if (!TextUtils.isEmpty(value)) {
                    commonSettingCmdResult.reason = value
                }
            }
        }
        return commonSettingCmdResult
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.UNKNOWN_TYPE
    }

    companion object {
        @JvmStatic
        val instance = CommonSettingCmdResultParser()
    }
}