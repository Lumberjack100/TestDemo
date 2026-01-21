package com.shmedo.lib.cmd.base.iot_cmd.parser.ml101

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.ml101.MS101SatParamInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：MS101 卫通参数信息解析器
 *
 * 解析 MD_CFG_SAT_PARAM (md_cfgsatparam) 指令的响应数据
 *
 * 指令响应格式：
 * ```
 * $cmd=md_cfgsatparam&method=0&status=1&sn=0001836&cesq=-10&csq=-66&waitnum=240&cregmode=0&cpsmmode=0&cpsmlevel=9&svmdmode=0&cclrmode=0
 * ```
 *
 * 参数说明：
 * - method: 功能码 (0-查询, 1-设置)
 * - status: 接入状态（0-已接入，1-未接入）
 * - sn: 产品序列号
 * - cesq: 卫星信号质量
 * - csq: 环境噪声信号强度
 * - waitnum: 待发数据数量（0-480）
 */
@IOTParser
class MS101SatParamParser : IOTCommandParser<MS101SatParamInfo> {

    /**
     * 解析键值对为 MS101SatParamInfo 对象
     *
     * @param keyValueMap 从指令响应中解析的键值对 Map
     * @return MS101SatParamInfo 卫通参数信息对象
     */
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): MS101SatParamInfo {
        return MS101SatParamInfo(
            status = keyValueMap["status"]?.toIntOrNull() ?: 0,
            sn = keyValueMap.getOrDefault("sn", ""),
            cesq = keyValueMap.getOrDefault("cesq", "-128"),
            csq = keyValueMap.getOrDefault("csq", ""),
            waitnum = keyValueMap["waitnum"]?.toIntOrNull() ?: 0
        )
    }

    /**
     * 指令类型：查询/设置卫通参数
     */
    override val commandType: IOTCommandType = IOTCommandType.MD_CFG_SAT_PARAM
}
