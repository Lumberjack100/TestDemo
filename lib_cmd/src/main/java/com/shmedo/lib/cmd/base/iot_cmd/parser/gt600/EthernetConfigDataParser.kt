package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.EthernetConfigData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: 以太网配置响应解析器
 *
 * 对应指令: md_geteth0
 * 应答示例: $cmd=md_geteth0&dhcp=0&ip=172.168.5.241&gateway=172.168.5.254&dns=8.8.8.8
 */
@IOTParser
class EthernetConfigDataParser : IOTCommandParser<EthernetConfigData> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): EthernetConfigData {
        return EthernetConfigData().apply {
            dhcp = keyValueMap.getOrDefault("dhcp", dhcp)
            ip = keyValueMap.getOrDefault("ip", ip)
            gateway = keyValueMap.getOrDefault("gateway", gateway)
            dns = keyValueMap.getOrDefault("dns", dns)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_ETHERNET
}
