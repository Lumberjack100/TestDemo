package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRInterfaceStatusInfo

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
class MRInterfaceStatusInfoParser {
    fun parseInstance(keyValueMap: Map<String, String>): MRInterfaceStatusInfo {
        return MRInterfaceStatusInfo().apply {
            rs485_1 = keyValueMap.getOrDefault("rs485_1", rs485_1)
            rs485_2 = keyValueMap.getOrDefault("rs485_2", rs485_2)
            rs485_3 = keyValueMap.getOrDefault("rs485_3", rs485_3)
            rs232_1 = keyValueMap.getOrDefault("rs232_1", rs232_1)
            rs232_2 = keyValueMap.getOrDefault("rs232_2", rs232_2)
            adc_a1 = keyValueMap.getOrDefault("adc_a1", adc_a1)
            adc_a2 = keyValueMap.getOrDefault("adc_a2", adc_a2)
            adc_a3 = keyValueMap.getOrDefault("adc_a3", adc_a3)
            adc_a4 = keyValueMap.getOrDefault("adc_a4", adc_a4)
            adc_v1 = keyValueMap.getOrDefault("adc_v1", adc_v1)
            adc_v2 = keyValueMap.getOrDefault("adc_v2", adc_v2)
        }
    }
}