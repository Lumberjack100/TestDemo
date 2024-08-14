package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRIOStatusInfo

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
class MRIOStatusInfoParser {
    fun parseInstance(keyValueMap: Map<String, String>): MRIOStatusInfo{
        return MRIOStatusInfo().apply {
            k1 = keyValueMap.getOrDefault("k1", k1)
            k2 = keyValueMap.getOrDefault("k2", k2)
            k3 = keyValueMap.getOrDefault("k3", k3)
            k4 = keyValueMap.getOrDefault("k4", k4)
            k5 = keyValueMap.getOrDefault("k5", k5)
            k6 = keyValueMap.getOrDefault("k6", k6)
            k7 = keyValueMap.getOrDefault("k7", k7)
            k8 = keyValueMap.getOrDefault("k8", k8)
            in1 = keyValueMap.getOrDefault("in1", in1)
            in2 = keyValueMap.getOrDefault("in2", in2)
            in3 = keyValueMap.getOrDefault("in3", in3)
            in4 = keyValueMap.getOrDefault("in4", in4)
            in5 = keyValueMap.getOrDefault("in5", in5)
            in6 = keyValueMap.getOrDefault("in6", in6)
            in7 = keyValueMap.getOrDefault("in7", in7)
            in8 = keyValueMap.getOrDefault("in8", in8)
            rain = keyValueMap.getOrDefault("rain", rain)
            dry = keyValueMap.getOrDefault("dry", dry)
        }
    }
}