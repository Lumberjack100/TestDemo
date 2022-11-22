package com.shmedo.configlibrary.iot.cmd.parser.e40

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/24 <br></br>
 * 描述：   解析E40卫星状态数据
 */
class E40SatelitteInfoParser : IOTResultParser<String?> {
    override fun parse(result: String): String? {
        val info: String
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
            info =  keyValueMap.getOrDefault("satelitte", "")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.E40_MD_GET_SATELITTE
    }
}