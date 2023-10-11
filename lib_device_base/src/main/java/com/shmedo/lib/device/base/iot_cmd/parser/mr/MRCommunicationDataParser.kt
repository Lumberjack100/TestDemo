package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.model.mr.MRCommunicationData

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/10 <br/>
 * 描述：     TODO
 */
class MRCommunicationDataParser {
    fun parseInstance(keyValueMap: Map<String, String>): MRCommunicationData {
        return MRCommunicationData(
            status1 = keyValueMap["status1"] ?: "",
            status2 = keyValueMap["status2"] ?: "",
            status3 = keyValueMap["status3"] ?: "",
            status4 = keyValueMap["status4"] ?: "",
            status5 = keyValueMap["status5"] ?: "",
            agreem1 = keyValueMap["agreem1"] ?: "",
            agreem2 = keyValueMap["agreem2"] ?: "",
            agreem3 = keyValueMap["agreem3"] ?: "",
            agreem4 = keyValueMap["agreem4"] ?: "",
            agreem5 = keyValueMap["agreem5"] ?: "",
            sdata1 = keyValueMap["sdata1"] ?: "",
            sdata2 = keyValueMap["sdata2"] ?: "",
            sdata3 = keyValueMap["sdata3"] ?: "",
            sdata4 = keyValueMap["sdata4"] ?: "",
            sdata5 = keyValueMap["sdata5"] ?: "",
            ndata1 = keyValueMap["ndata1"] ?: "",
            ndata2 = keyValueMap["ndata2"] ?: "",
            ndata3 = keyValueMap["ndata3"] ?: "",
            ndata4 = keyValueMap["ndata4"] ?: "",
            ndata5 = keyValueMap["ndata5"] ?: "",
            rate1 = keyValueMap["rate1"] ?: "",
            rate2 = keyValueMap["rate2"] ?: "",
            rate3 = keyValueMap["rate3"] ?: "",
            rate4 = keyValueMap["rate4"] ?: "",
            rate5 = keyValueMap["rate5"] ?: "",
        )
    }
}