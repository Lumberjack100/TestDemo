package com.shmedo.lib.cmd.base.md_cmd.assemble.entity.das

import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： 配置认证方式的参数
 */
data class AuthenticationEntity(
    var sn: String = "",//采集器型号
    var mode: String = "",//认证方式 0:普通认证 1:系统认证
) {
    fun toCommandString(): String {

        return "${MDConstants.COMMAND_SPLICER_COMMA}$sn${MDConstants.COMMAND_SPLICER_COMMA}$mode"
    }
}

