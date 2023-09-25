package com.shmedo.lib.device.base.iot_cmd.parser.vms

import android.text.TextUtils
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.vms.VmsTerminalSn

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/10/12 <br></br>
 * 描述：     TODO
 */
class VmsTerminalSnParserOld :
    OldIOTResultParser<VmsTerminalSn?> {
    override fun parse(result: String): VmsTerminalSn? {
        val info = VmsTerminalSn()
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
            info.sn = if (TextUtils.isEmpty(keyValueMap["sn"])) "" else keyValueMap["sn"]!!
                .replace("\"", "")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.VMS_MD_GET_TERMINAL_SN
    }
}