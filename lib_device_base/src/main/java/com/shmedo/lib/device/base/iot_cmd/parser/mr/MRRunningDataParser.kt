package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRunningData

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/10 <br/>
 * 描述：     TODO
 */
class MRRunningDataParser {
    fun parseInstance(keyValueMap: Map<String, String>): MRRunningData {
        return MRRunningData().apply {
            ttime = keyValueMap.getOrDefault("ttime", ttime)
            otime = keyValueMap.getOrDefault("otime", otime)
            rebootn = keyValueMap.getOrDefault("rebootn", rebootn)
            ustorage = keyValueMap.getOrDefault("ustorage", ustorage)
            tstorage = keyValueMap.getOrDefault("tstorage", tstorage)
        }
    }
}