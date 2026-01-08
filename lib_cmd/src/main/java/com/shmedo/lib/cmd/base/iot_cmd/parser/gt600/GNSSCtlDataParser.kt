package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.GNSSCtlData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: GNSS 控制参数（RTCM参数）解析器
 *
 * 对应指令: md_getgnssctl
 * 应答示例: $cmd=md_getgnssctl&encrypttype=0&rtcmobstime=15&rtcmephtime=60&onlybd=0
 */
@IOTParser
class GNSSCtlDataParser : IOTCommandParser<GNSSCtlData> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): GNSSCtlData {
        return GNSSCtlData().apply {
            encrypttype = keyValueMap.getOrDefault("encrypttype", encrypttype)
            rtcmobstime = keyValueMap.getOrDefault("rtcmobstime", rtcmobstime)
            rtcmephtime = keyValueMap.getOrDefault("rtcmephtime", rtcmephtime)
            onlybd = keyValueMap.getOrDefault("onlybd", onlybd)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_GNSS_CTL
}
