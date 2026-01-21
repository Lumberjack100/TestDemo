package com.shmedo.lib.cmd.base.iot_cmd.parser.mg301

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.mg301.MG301SatModInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2026/01/19
 * 描述：MG301 卫通模块状态信息解析器
 *
 * 解析 MD_GET_SATMOD (md_getsatmod) 指令的响应数据
 *
 * 指令响应格式：
 * ```
 * $cmd=md_getsatmod&status=1&sn=0001836&cesq=-10&csq=-66&waitnum=200
 * ```
 *
 * 参数说明：
 * - status: 接入状态（0-未接入，1-已接入）
 * - sn: 产品序列号
 * - cesq: 卫星信号质量
 * - csq: 环境噪声信号强度
 * - waitnum: 待发数据数量（1-480）
 */
@IOTParser
class MG301SatModParser : IOTCommandParser<MG301SatModInfo> {

    /**
     * 解析键值对为 MG301SatModInfo 对象
     *
     * @param keyValueMap 从指令响应中解析的键值对 Map
     * @return MG301SatModInfo 卫通模块状态信息对象
     */
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MG301SatModInfo {
        return MG301SatModInfo(
            status = keyValueMap["status"]?.toIntOrNull() ?: 0,
            sn = keyValueMap.getOrDefault("sn", ""),
            cesq = keyValueMap.getOrDefault("cesq", "-128"),
            csq = keyValueMap.getOrDefault("csq", ""),
            waitnum = keyValueMap["waitnum"]?.toIntOrNull() ?: 0
        )
    }

    /**
     * 指令类型：获取卫通模块状态
     */
    override val commandType: IOTCommandType = IOTCommandType.MD_GET_SATMOD
}
