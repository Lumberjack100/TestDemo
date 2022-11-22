package com.shmedo.configlibrary.iot.cmd.parser.vms

import com.blankj.utilcode.util.GsonUtils
import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.vms.VmsAisleTerminalInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/11/13 <br></br>
 * 描述：     TODO #gh#
 */
class VmsAisleTerminalInfoParser : IOTResultParser<VmsAisleTerminalInfo?> {
    override fun parse(result: String): VmsAisleTerminalInfo? {
        val info: VmsAisleTerminalInfo
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
            val status = keyValueMap["status"]
            info = GsonUtils.fromJson(status, VmsAisleTerminalInfo::class.java)
            info.channel = keyValueMap["channel"]?.toInt() ?: 0
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.VMS_MD_GET_TERMINAL_STATUS
    }
}