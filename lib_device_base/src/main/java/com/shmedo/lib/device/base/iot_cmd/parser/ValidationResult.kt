package com.shmedo.lib.device.base.iot_cmd.parser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：    指令校验结果
 */
data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)
