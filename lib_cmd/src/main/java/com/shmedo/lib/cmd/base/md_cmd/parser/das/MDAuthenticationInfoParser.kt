package com.shmedo.lib.cmd.base.md_cmd.parser.das

import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.model.das.AuthenticationInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述：设备认证方式解析器
 */
class MDAuthenticationInfoParser : MDCommandParser<AuthenticationInfo> {
    override fun parseInstance(values: List<String>): AuthenticationInfo {
        return AuthenticationInfo().apply {
            sn = values.getOrNull(1) ?: sn
            mode = values.getOrNull(2) ?: mode
            publicKey = values.getOrNull(3) ?: publicKey
        }
    }

    override fun commandType(): MDCommandType = MDCommandType.AUTHENTICATION_CONFIG
}