package com.shmedo.lib.cmd.base.md_cmd.assemble.entity.common

import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/4/12
 * 描述： TODO
 */
data class ServerAddressInfoEntity(
    var centerid: String = "",//服务器(数据中心)编号，取值1,2,3
    var address: String = "",//服务器地址（可以为IP或域名）
    var port: String = "",//服务器端口（最大65535）
) {
    fun toCommandString(): String {

        return "$centerid${MDConstants.COMMAND_SPLICER_WHITESPACE}$address${MDConstants.COMMAND_SPLICER_WHITESPACE}$port"
    }
}