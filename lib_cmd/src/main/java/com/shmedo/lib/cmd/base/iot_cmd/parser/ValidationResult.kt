package com.shmedo.lib.cmd.base.iot_cmd.parser

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：    指令校验结果
 */

/**
 * 验证结果数据类
 * @property isValid 是否验证通过
 * @property errorMessage 错误信息,验证失败时不为空
 */
data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)
