package com.shmedo.lib.device.base.md_cmd.parser.common

import com.shmedo.lib.device.base.iot_cmd.parser.ParseResult
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.model.common.ServerAddressInfo
import com.shmedo.lib.device.base.md_cmd.utils.MDConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/4/12
 * 描述： TODO
 * $$2003 httphub.shmedo.cn 1883
 */
class MDServerAddressInfoParser: MDCommandParser<ServerAddressInfo> {
    override fun parse(result: String): ParseResult<ServerAddressInfo> {
        return try {
            val values = result.replace("\r\n", "").split(MDConstants.COMMAND_SPLICER_WHITESPACE)
            // 子类实现
            ParseResult.Success(parseInstance(values))
        } catch (ex: Exception) {
            ParseResult.Failure("解析错误: ${ex.message ?: "Unknown error"}")
        }
    }


    override fun parseInstance(values: List<String>): ServerAddressInfo {
        return ServerAddressInfo().apply {
            centerid = values.getOrNull(0)?.substring(5) ?: centerid
            addr = values.getOrNull(1) ?: addr
            port = values.getOrNull(2) ?: port
        }
    }


    override fun commandType(): MDCommandType = MDCommandType.SERVER_ADDRESS
}