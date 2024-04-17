package com.shmedo.lib.device.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： 米度设备认证信息
 */
data class AuthenticationInfo(
    var sn: String = "",//SN号
    var mode: String = "",//认证方式 0:普通认证 1:系统认证
    var publicKey: String = "",//公钥
)