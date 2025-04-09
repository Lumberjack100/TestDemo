package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRWiredNet
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     TODO
 */
@IOTParser
class MRWiredNetParser : IOTCommandParser<MRWiredNet> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MRWiredNet {
        return MRWiredNet().apply {
            switch = keyValueMap.getOrDefault("switch", switch)
            dhcp = keyValueMap.getOrDefault("dhcp", dhcp)
            ipaddr = keyValueMap.getOrDefault("ipaddr", ipaddr)
            mask = keyValueMap.getOrDefault("mask", mask)
            gateway = keyValueMap.getOrDefault("gateway", gateway)
            dns = keyValueMap.getOrDefault("dns", dns)
            dnss = keyValueMap.getOrDefault("dnss", dnss)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MR_MD_GET_WIRELESS_NETWORK
}