package com.shmedo.lib.cmd.base.md_cmd.assemble.entity.common

import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/4/12
 * 描述： TODO
 */
data class RegistrationPlatformEntity(
    var centerid: String = "",//服务器(数据中心)编号，取值1,2,3
    var sNOrProductId: String = "",//设备SN号/产品ID
    var productIdOrDeviceId: String = "",//产品ID/设备ID
    var registrationCodeOrPwd: String = "",//注册码/设备KEY
){
    fun toCommandString(): String {

        return "$centerid$sNOrProductId${MDConstants.COMMAND_SPLICER_COMMA}$productIdOrDeviceId${MDConstants.COMMAND_SPLICER_COMMA}$registrationCodeOrPwd"
    }
}
