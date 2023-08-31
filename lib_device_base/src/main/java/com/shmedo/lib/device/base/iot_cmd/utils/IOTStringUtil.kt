package com.shmedo.lib.device.base.iot_cmd.utils

import android.text.TextUtils
import com.shmedo.lib.device.base.iot_cmd.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/31 <br></br>
 * 描述：     TODO #gh#
 */
object IOTStringUtil {
    /**
     * 从DAS返回结果中提取指令类型
     *
     * @param result $cmd=reqtime&time=2020-08-31 14:27:21&apikey=80c8b131-80e5-4504-bb67-72e8cf93a542&msgid=63d30270-80f6-4458-bafb-c669585f5e4b
     * @return 从DAS返回结果中提取指令类型
     */
    @JvmStatic
    fun extractCommandType(result: String): IOTCommandType {
        if (TextUtils.isEmpty(result) || result.length < IOTCommandResult.RESULT_MIN_LENGTH) {
            return IOTCommandType.LENGTH_INVALID
        }
        val strs = result.split("&").toTypedArray()
        val cmd = strs[0].replace(IOTCommandResult.COMMAND_HEADER, "").trim { it <= ' ' }
        val cmdTypeHolder = Holder<IOTCommandType>()
        for (commandType in IOTCommandType.values()) {
            if (commandType.toString() == cmd) {
                cmdTypeHolder.data = commandType
                break
            }
        }
        val cmdType = cmdTypeHolder.data
        return cmdType ?: IOTCommandType.UNKNOWN_TYPE
    }
}