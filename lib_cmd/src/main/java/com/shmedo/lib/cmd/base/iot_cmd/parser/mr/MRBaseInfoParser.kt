package com.shmedo.lib.cmd.base.iot_cmd.parser.mr

import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRBaseInfo

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/9 <br/>
 * 描述：     TODO
 */
class MRBaseInfoParser {
    fun parseInstance(keyValueMap: Map<String, String>): MRBaseInfo {
        return MRBaseInfo().apply {
            productname = keyValueMap.getOrDefault("productname", productname)
            producttype = keyValueMap.getOrDefault("producttype", producttype)
            regcode = keyValueMap.getOrDefault("regcode", regcode)
            sn = keyValueMap.getOrDefault("sn", sn)
            ver = keyValueMap.getOrDefault("ver", ver)
            imei = keyValueMap.getOrDefault("imei", imei)
            iccid = keyValueMap.getOrDefault("iccid", iccid)
            temp = keyValueMap.getOrDefault("temp", temp)
            hum = keyValueMap.getOrDefault("hum", hum)
            volt = keyValueMap.getOrDefault("volt", volt)
            csq = keyValueMap.getOrDefault("csq", csq)
            local = keyValueMap.getOrDefault("local", local)
            regtime = keyValueMap.getOrDefault("regtime", regtime)
        }
    }
}