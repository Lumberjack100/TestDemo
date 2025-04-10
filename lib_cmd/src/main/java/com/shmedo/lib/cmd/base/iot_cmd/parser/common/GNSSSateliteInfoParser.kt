package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     解析卫星信息
 */
@IOTParser
class GNSSSateliteInfoParser: IOTCommandParser<String> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): String {
        return keyValueMap["sateliteinfo"]!!
    }

    override val commandType: IOTCommandType = IOTCommandType.M50_MD_GET_SATELITE_INFO
}