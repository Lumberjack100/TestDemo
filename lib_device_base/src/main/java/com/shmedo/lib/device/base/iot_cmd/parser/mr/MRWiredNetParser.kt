package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRWiredNet

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     TODO
 */
class MRWiredNetParser : IOTCommandParser<MRWiredNet> {

    override fun parseInstance(keyValueMap: Map<String, String>): MRWiredNet {
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

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_WIRED_NETWORK
}