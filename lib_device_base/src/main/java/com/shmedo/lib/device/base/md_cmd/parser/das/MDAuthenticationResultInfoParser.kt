package com.shmedo.lib.device.base.md_cmd.parser.das

import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.model.das.AuthenticationResultInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述：设备认证方式解析器
 */
class MDAuthenticationResultInfoParser : MDCommandParser<AuthenticationResultInfo> {
    override fun parseInstance(values: List<String>): AuthenticationResultInfo {
        return AuthenticationResultInfo().apply {
            result = values.getOrNull(1) ?: result
        }
    }

    override fun commandType(): MDCommandType = MDCommandType.DAS_SEND_AUTHENTICATION_RESULT
}