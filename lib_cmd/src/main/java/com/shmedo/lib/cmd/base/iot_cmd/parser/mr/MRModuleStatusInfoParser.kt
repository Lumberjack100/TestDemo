package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRModuleStatusInfo

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/10 <br/>
 * 描述：     TODO
 */
class MRModuleStatusInfoParser {
    fun parseInstance(keyValueMap: Map<String, String>): MRModuleStatusInfo {
        return MRModuleStatusInfo().apply {
            screen = keyValueMap.getOrDefault("screen", screen)
            datanet = keyValueMap.getOrDefault("datanet", datanet)
            beidou = keyValueMap.getOrDefault("beidou", beidou)
            wirednet = keyValueMap.getOrDefault("wirednet", wirednet)
            flash = keyValueMap.getOrDefault("flash", flash)
            emmc = keyValueMap.getOrDefault("emmc", emmc)
        }
    }
}